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

import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Expression;

import java.io.IOException;
import java.util.*;

import static boa.functions.BoaAstIntrinsics.parseexpression;
import static boa.functions.BoaAstIntrinsics.prettyprint;

/**
 * @author marafat
 * @author jsu
 */
@AggregatorSpec(name = "precondition", formalParameters = { "float" })
public class PreconditionAggregator extends Aggregator {
	private double sigma = 0.5;
	private int args = 0;

	private Map<Expression, Set<String>> precondMethods;
	private Map<Expression, Set<String>> precondProjects;

	/**
	 * Construct a {@link PreconditionAggregator}
	 */
	public PreconditionAggregator() {
		this(0.5);
	}

	public PreconditionAggregator(final double sigma) {
		this.sigma = sigma;
		this.precondMethods = new HashMap<Expression, Set<String>>();  //preconditions: set of methods
		this.precondProjects = new HashMap<Expression, Set<String>>(); //preconditions: set of projects
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException, FinishedException {
		//data expected format: "no_of_args:pid:fq_clientmethodname:precondition"

		String[] sData = data.split(":", 4);

		if(Integer.parseInt(sData[0]) > this.args)
			this.args = Integer.parseInt(sData[0]);
		final String project = sData[1];
		final String clientmethod = sData[2];
		final String precond = sData[3];

		final Expression precondition = parseexpression(precond);

		if (!precondMethods.containsKey(precondition)) {
			precondMethods.put(precondition, new HashSet<String>());
			precondProjects.put(precondition, new HashSet<String>());
		}

		precondMethods.get(precondition).add(clientmethod);
		precondProjects.get(precondition).add(project);
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		doInference();

		final Map<String, Double> filteredPreconds = doFiltering();
		final List<Map.Entry<String, Double>> rankedPreconds = doRanking(filteredPreconds);

		//Send to Writer
		for (final Map.Entry<String, Double> precondConf : rankedPreconds){
			this.collect(precondConf.getKey()+": "+precondConf.getValue());
		}
	}

	/**
	 *  Infer preconditions for both projects and method calls
	 */
	private void doInference(){
		precondMethods = infer(precondMethods);
		precondProjects = infer(precondProjects);
	}

	/**
	 * Infer the weak preconditions from the mined preconditions.
	 *
	 * @return map
	 */
	private Map<Expression, Set<String>> infer(Map<Expression, Set<String>> precondMP) {
		final Map<Expression, Set<String>> infPreconditions = new HashMap<Expression, Set<String>>(precondMP);
		final Set<Expression> preconds = new HashSet<Expression>(infPreconditions.keySet());
		//int count1 = 0;
		//int count2 = 0;

		for (final Expression eqPrecond : preconds) {
			if (eqPrecond.getKind() == ExpressionKind.EQ) {
				for (final Expression sineqPrecond : preconds) {
					if (sineqPrecond.getKind() == ExpressionKind.LT || sineqPrecond.getKind() == ExpressionKind.GT) {
						if (eqPrecond.getExpressions(0).equals(sineqPrecond.getExpressions(0)) &&
									eqPrecond.getExpressions(1).equals(sineqPrecond.getExpressions(1))) {

							//count1++;
							Expression nsineqPrecond;
							final Expression lhs = sineqPrecond.getExpressions(0);
							final Expression rhs = sineqPrecond.getExpressions(1);

							if (sineqPrecond.getKind() == ExpressionKind.GT) {
								nsineqPrecond = parseexpression(prettyprint(lhs) + ">=" + prettyprint(rhs));

								if (!containsExp(preconds, nsineqPrecond))
									infPreconditions.put(nsineqPrecond, new HashSet<String>());

							} else {
								nsineqPrecond = parseexpression(prettyprint(lhs) + "<=" + prettyprint(rhs));

								if (!containsExp(preconds, nsineqPrecond))
									infPreconditions.put(nsineqPrecond, new HashSet<String>());
							}

							if (infPreconditions.get(eqPrecond).size() == infPreconditions.get(sineqPrecond).size())
								infPreconditions.put(nsineqPrecond, union(infPreconditions.get(nsineqPrecond),
																	union(infPreconditions.get(eqPrecond),
																			infPreconditions.get(sineqPrecond))));
							else if (infPreconditions.get(eqPrecond).size() > infPreconditions.get(sineqPrecond).size())
								infPreconditions.put(nsineqPrecond, union(infPreconditions.get(nsineqPrecond),
																			infPreconditions.get(sineqPrecond)));
							else
								infPreconditions.put(nsineqPrecond, union(infPreconditions.get(nsineqPrecond),
																			infPreconditions.get(eqPrecond)));

							//Conditions with implications
							//if (infPreconditions.get(eqPrecond).size() <= infPreconditions.get(nsineqPrecond).size())
							//	count2++;

							if (infPreconditions.get(sineqPrecond).size() <= infPreconditions.get(nsineqPrecond).size())
								infPreconditions.get(sineqPrecond).clear();
						}
					}
				}

				//if (count2 == 2 || (count2 == 1 && count1 == 1))
					infPreconditions.get(eqPrecond).clear();  //not removing for consistency b/w methods and projects
			}
		}

		return infPreconditions;
	}

	/**
	 * Filter out the preconditions with confidence less than sigma
	 *
	 * @return filtered preconditons map
	 */
	private Map<String, Double> doFiltering() {
		final Map<String, Double> precondConfM = calConfidence(precondMethods);
		precondMethods.clear();
		final Map<String, Double> precondConfP = calConfidence(precondProjects);
		precondProjects.clear();

		final Map<String, Double> filteredPreconds = new HashMap<String, Double>();

		final Set<String> preconds = precondConfM.keySet();
		for (final String precond: preconds) {
			if (precondConfM.get(precond) >= sigma && precondConfP.get(precond) >= sigma)
				filteredPreconds.put(precond, precondConfM.get(precond)*precondConfP.get(precond));
		}

		return filteredPreconds;
	}

	/**
	 * Calculate confidence of each precondition in the map
	 *
	 * @param precondMP map of preconditon and set of clientmethods/projects
	 * @return map of precondition and confidence
	 */
	private Map<String, Double> calConfidence(final Map<Expression, Set<String>> precondMP) {
		final Map<String, Double> precondConf = new HashMap<String, Double>();
		final Set<Expression> preconds = precondMP.keySet();

		final Set<String> totalCalls = new HashSet<String>();
		for (final Expression precond : preconds)
			totalCalls.addAll(precondMP.get(precond));

		for (final Expression precond : preconds) {
			final Double conf = precondMP.get(precond).size() / (totalCalls.size() * 1.0);
			precondConf.put(prettyprint(precond), conf);
		}

		return precondConf;
	}

	/**
	 * Rank the preconditions based on their confidence value
	 *
	 * @param filteredPreconds map of filtered precondtion
	 * @return ranked preconditions
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private List<Map.Entry<String, Double>> doRanking(final Map<String, Double> filteredPreconds) throws IOException, InterruptedException {

		final Map<String, Double> finalPreconds = new HashMap<String, Double>();
		final Set<SortedSet<String>> argsComb = kCombinations();   //k = 2^args - 1

		for (SortedSet<String> s: argsComb) {
			final Map<String, Double> argPrecond = new HashMap<String, Double>();
			for (String precond: filteredPreconds.keySet()) {
				boolean allPresent = true;
				for (String arg : s) {
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
	 *
	 * @return set of all combinations of arguments
	 */
	private  Set<SortedSet<String>> kCombinations() {
		final Set<SortedSet<String>> comb = new HashSet<SortedSet<String>>();
		final List<String> argList = new ArrayList<String>();
		comb.add(new TreeSet<String>(Collections.singletonList("rcv$")));
		argList.add("rcv$");

		for (int i = 0; i < args; i++) {
			comb.add(new TreeSet<String>(Collections.singletonList("arg$" + Integer.toString(i))));
			argList.add("arg$" + Integer.toString(i));
		}

		for (String arg: argList) {
			final Set<SortedSet<String>> tempComb = new HashSet<SortedSet<String>>(comb);
			for (SortedSet<String> s: tempComb) {
				SortedSet<String> t = new TreeSet<String>(s);
				t.add(arg);
				comb.add(t);
			}
		}

		return comb;
	}


	/**
	 * Comparator to sort preconditions based on confidence values
	 *
	 */
	public class PreconditionComparator implements Comparator<Map.Entry<String, Double>> {
		public int compare(final Map.Entry<String, Double> p1, final Map.Entry<String, Double> p2) {
			return (p2.getValue()).compareTo(p1.getValue());
		}
	}

	/**
	 * Checks the presence of a particular ExpressionKind in a set.
	 *
	 * @param exprs set of expressions
	 * @param expr Expressionto be searched
	 * @return true if Expressionkind is present in the set
	 */
	private boolean containsExp(final Set<Expression> exprs, final Expression expr) {
		for (final Expression e : exprs) {
			if (expr.equals(e))
				return true;
		}

		return false;
	}

	/**
	 * Performs union operation on two given sets.
	 *
	 * @param s1
	 * @param s2
	 * @param <T>
	 * @return the union of two sets
	 */
	private <T> Set<T> union(final Set<T> s1, final Set<T> s2) {
		final Set<T> s = new HashSet<T>(s1);
		s.addAll(s2);
		return s;
	}

}
