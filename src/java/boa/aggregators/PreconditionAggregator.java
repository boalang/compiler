/*
 * Copyright 2018, Robert Dyer, Mohd Arafat, Jingyi Su
 *                 and Bowling Green State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.aggregators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static boa.functions.BoaAstIntrinsics.parseexpression;
import static boa.functions.BoaAstIntrinsics.prettyprint;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Expression;

import java.io.IOException;
import java.util.*;

import static boa.functions.BoaAstIntrinsics.*;
import boa.output.Output.Value;

/**
 * @author marafat
 * @author jsu
 * @author rdyer
 */
@AggregatorSpec(name = "precondition", formalParameters = { "float" })
public class PreconditionAggregator extends Aggregator {
	private final double sigma;
	private int args = 0;

	private final Set<Expression> preconds = new HashSet<Expression>();
	private final Map<Expression, Set<String>> omega = new HashMap<Expression, Set<String>>();  // preconditions: set of methods

	/**
	 * Construct a {@link PreconditionAggregator}
	 */
	public PreconditionAggregator() {
		this(0.5);
	}

	/**
	 * Construct a {@link PreconditionAggregator}
	 *
	 * @param sigma the cutoff sigma value to use
	 */
	public PreconditionAggregator(final double sigma) {
		this.sigma = sigma;
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final Value metadata) throws IOException, InterruptedException, FinishedException {
		// data expected format: "no_of_args:pid:fq_clientmethodname:precondition"
		final String[] sData = data.split(":", 4);

		final int newArgs = Integer.parseInt(sData[0]);
		if (newArgs > this.args)
			this.args = newArgs;

		final String project = sData[1];
		final String clientmethod = sData[2];
		final String precond = sData[3];

		final Expression precondition = parseexpression(precond);

		if (!omega.containsKey(precondition)) {
			omega.put(precondition, new HashSet<String>());
			preconds.add(precondition);
		}

		omega.get(precondition).add(project + ":" + clientmethod);
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		inferNonStrictInequalities();
		mergeConditionsWithImplication();

		for (final Map.Entry<String, Double> precondConf : doRanking(doFiltering())) {
			this.collect(precondConf.getKey() + ": " + precondConf.getValue());
		}

		preconds.clear();
		omega.clear();
	}

	/**
	 * Infers the weak preconditions from the mined preconditions.
	 *
	 * @return map of all preconditions which include inferred preconditions
	 */
	private void inferNonStrictInequalities() {
		final Set<Expression> added = new HashSet<Expression>();

		for (final Expression p : preconds) {
			if (p.getKind() == ExpressionKind.EQ) {
				for (final Expression q : preconds) {
					if (q.getKind() == ExpressionKind.LT || q.getKind() == ExpressionKind.GT) {
						if (p.getExpressions(0).equals(q.getExpressions(0)) && p.getExpressions(1).equals(q.getExpressions(1))) {
							final Expression.Builder builder = Expression.newBuilder(q);

							if (q.getKind() == ExpressionKind.GT)
								builder.setKind(ExpressionKind.GTEQ);
							else
								builder.setKind(ExpressionKind.LTEQ);

							final Expression t = builder.build();

							if (!preconds.contains(t)) {
								omega.put(t, new HashSet<String>());
								added.add(t);
							}

							if (omega.get(p).size() == omega.get(q).size()) {
								omega.get(t).addAll(omega.get(p));
								omega.get(t).addAll(omega.get(q));
							} else if (omega.get(p).size() > omega.get(q).size()) {
								omega.get(t).addAll(omega.get(q));
							} else {
								omega.get(t).addAll(omega.get(p));
							}
						}
					}
				}
			}
		}

		preconds.addAll(added);
	}

	/**
	 * Merges preconditions with implications
	 *
	 * @return map of precondition with "precondition with implication" merged
	 */
	private void mergeConditionsWithImplication() {
		final List<Expression> removed = new ArrayList<Expression>();

		for (final Expression p : preconds) {
			if (p.getKind() == ExpressionKind.EQ || p.getKind() == ExpressionKind.LT || p.getKind() == ExpressionKind.GT) {
				for (final Expression q : preconds) {
					if ((p.getKind() == ExpressionKind.EQ && q.getKind() == ExpressionKind.LTEQ)
							|| (p.getKind() == ExpressionKind.EQ && q.getKind() == ExpressionKind.GTEQ)
							|| (p.getKind() == ExpressionKind.LT && q.getKind() == ExpressionKind.LTEQ)
							|| (p.getKind() == ExpressionKind.GT && q.getKind() == ExpressionKind.GTEQ)) {
						if (omega.get(p).size() <= omega.get(q).size()
								&& p.getExpressions(0).equals(q.getExpressions(0))
								&& p.getExpressions(1).equals(q.getExpressions(1))) {
							omega.get(q).addAll(omega.get(p));
							omega.remove(p);
							removed.add(p);
							break;
						}
					}
				}
			}
		}

		preconds.removeAll(removed);
	}

	/**
	 * Filters out the preconditions with confidence less than sigma
	 *
	 * @return filtered preconditons map
	 */
	private Map<String, Double> doFiltering() {
		final Map<Expression, Double> precondConfM = calcConfidence(omega);

		// generate psi from omega
		final Map<Expression, Set<String>> psi = new HashMap<Expression, Set<String>>();
		for (final Expression e : preconds) {
			psi.put(e, new HashSet<String>());

			for (final String s : omega.get(e)) {
				psi.get(e).add(s.split(":", 2)[0]);
			}
		}

		final Map<Expression, Double> precondConfP = calcConfidence(psi);

		final Map<String, Double> filteredPreconds = new HashMap<String, Double>();

		for (final Expression precond : precondConfM.keySet())
			if (precondConfM.get(precond) >= sigma && precondConfP.get(precond) >= sigma)
				filteredPreconds.put(prettyprint(precond), precondConfM.get(precond) * precondConfP.get(precond));

		return filteredPreconds;
	}

	/**
	 * Calculates confidence for each precondition in the map
	 *
	 * @param precondMP map of preconditon and set of clientmethods/projects
	 * @return map of precondition and confidence
	 */
	private Map<Expression, Double> calcConfidence(final Map<Expression, Set<String>> precondMP) {
		final Map<Expression, Double> precondConf = new HashMap<Expression, Double>();

		final Set<String> totalCalls = new HashSet<String>();
		for (final Expression precond : preconds)
			totalCalls.addAll(precondMP.get(precond));

		for (final Expression precond : preconds)
			precondConf.put(precond, precondMP.get(precond).size() / (double)totalCalls.size());

		return precondConf;
	}

	/**
	 * Ranks the preconditions based on their confidence value
	 *
	 * @param filteredPreconds map of filtered precondtion
	 * @return ranked preconditions
	 */
	private List<Map.Entry<String, Double>> doRanking(final Map<String, Double> filteredPreconds) {
		final Map<String, Double> finalPreconds = new HashMap<String, Double>();
		final Set<SortedSet<String>> argsComb = kCombinations(); // k = 2^(args+1) - 1

		for (final SortedSet<String> s : argsComb) {
			final Map<String, Double> argPrecond = new HashMap<String, Double>();
			for (final String precond : filteredPreconds.keySet()) {
				boolean allPresent = true;
				for (final String arg : s) {
					if (!precond.contains(arg)) {
						allPresent = false;
						break;
					}
				}

				if (allPresent)
					argPrecond.put(precond, filteredPreconds.get(precond));
			}

			if (argPrecond.size() > 0) {
				final List<Map.Entry<String, Double>> topPrecond = new ArrayList<Map.Entry<String, Double>>(argPrecond.entrySet());
				Collections.sort(topPrecond, new PreconditionComparator());
				finalPreconds.put(topPrecond.get(0).getKey(), topPrecond.get(0).getValue());
			}
		}

		final List<Map.Entry<String, Double>> rankedPreconds = new ArrayList<Map.Entry<String, Double>>(finalPreconds.entrySet());
		Collections.sort(rankedPreconds, new PreconditionComparator());

		return rankedPreconds;
	}

	/**
	 * Generates all possible combinations of arguments and reciever: k = 2^(args+1) - 1
	 *
	 * @return set of all combinations of arguments
	 */
	private Set<SortedSet<String>> kCombinations() {
		final Set<SortedSet<String>> comb = new HashSet<SortedSet<String>>();
		final List<String> argList = new ArrayList<String>();
		comb.add(new TreeSet<String>(Collections.singletonList("$RECEIVER$")));
		argList.add("$RECEIVER$");

		for (int i = 0; i < args; i++) {
			comb.add(new TreeSet<String>(Collections.singletonList("$ARG$" + Integer.toString(i))));
			argList.add("$ARG$" + Integer.toString(i));
		}

		for (final String arg : argList) {
			final Set<SortedSet<String>> tempComb = new HashSet<SortedSet<String>>(comb);
			for (final SortedSet<String> s : tempComb) {
				final SortedSet<String> t = new TreeSet<String>(s);
				t.add(arg);
				comb.add(t);
			}
		}

		return comb;
	}

	/**
	 * Comparator to sort preconditions based on confidence values
	 */
	public class PreconditionComparator implements Comparator<Map.Entry<String, Double>> {
		public int compare(final Map.Entry<String, Double> p1, final Map.Entry<String, Double> p2) {
			return p2.getValue().compareTo(p1.getValue());
		}
	}
}
