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

import static boa.functions.BoaAstIntrinsics.*;

/**
 * @author marafat
 * @author jsu
 * @author rdyer
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
		// data expected format: "no_of_args:pid:fq_clientmethodname:precondition"
		final String[] sData = data.split(":", 4);

		if (Integer.parseInt(sData[0]) > this.args)
			this.args = Integer.parseInt(sData[0]);

		final String project = sData[1];
		final String clientmethod = sData[2];
		final String precond = sData[3];

		final Expression precondition = parseexpression(precond);

		if (!precondMethods.containsKey(precondition)) {
			precondMethods.put(precondition, new HashSet<String>());
			precondProjects.put(precondition, new HashSet<String>());
		}

		precondMethods.get(precondition).add(project + clientmethod);
		precondProjects.get(precondition).add(project);
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		doInference();

		final Map<String, Double> filteredPreconds = doFiltering();
		final List<Map.Entry<String, Double>> rankedPreconds = doRanking(filteredPreconds);

		// send to Writer
		for (final Map.Entry<String, Double> precondConf : rankedPreconds) {
			this.collect(precondConf.getKey()+": "+precondConf.getValue());
		}
	}

	/**
	 * Infers preconditions for both projects and method calls
	 */
	private void doInference() {
		precondMethods = removeEquality(mergeConditionsWithImplication(infer(precondMethods)));
		precondProjects = removeEquality(mergeConditionsWithImplication(infer(precondProjects)));
	}

	/**
	 * Infers the weak preconditions from the mined preconditions.
	 *
	 * @return map of all preconditions which include inferred preconditions
	 */
	private Map<Expression, Set<String>> infer(Map<Expression, Set<String>> precondMP) {
		final Map<Expression, Set<String>> infPreconditions = new HashMap<Expression, Set<String>>(precondMP);
		final Set<Expression> preconds = new HashSet<Expression>(infPreconditions.keySet());

		for (final Expression eqPrecond : preconds) {
			if (eqPrecond.getKind() == ExpressionKind.EQ) {
				for (final Expression sineqPrecond : preconds) {
					if (sineqPrecond.getKind() == ExpressionKind.LT || sineqPrecond.getKind() == ExpressionKind.GT) {
						if (eqPrecond.getExpressions(0).equals(sineqPrecond.getExpressions(0)) &&
									eqPrecond.getExpressions(1).equals(sineqPrecond.getExpressions(1))) {
							final Expression.Builder builder = Expression.newBuilder(sineqPrecond);

							if (sineqPrecond.getKind() == ExpressionKind.GT)
								builder.setKind(ExpressionKind.GTEQ);
							else
								builder.setKind(ExpressionKind.LTEQ);

							final Expression nsineqPrecond = builder.build();

							if (!preconds.contains(nsineqPrecond))
								infPreconditions.put(nsineqPrecond, new HashSet<String>());

							if (infPreconditions.get(eqPrecond).size() == infPreconditions.get(sineqPrecond).size()) {
								Set<String> tempSet = new HashSet<String>(infPreconditions.get(nsineqPrecond));
								tempSet.addAll(infPreconditions.get(eqPrecond));
								infPreconditions.get(nsineqPrecond).addAll(tempSet);
							}
							else if (infPreconditions.get(eqPrecond).size() > infPreconditions.get(sineqPrecond).size())
								infPreconditions.get(nsineqPrecond).addAll(infPreconditions.get(sineqPrecond));
							else
								infPreconditions.get(nsineqPrecond).addAll(infPreconditions.get(eqPrecond));

						}
					}
				}
			}
		}

		return infPreconditions;
	}

	/**
	 * Merges preconditions with implications
	 *
	 * @param precondMP inferred Preconditions
	 * @return map of precondition with "precondition with implication" merged
	 */

	private Map<Expression, Set<String>> mergeConditionsWithImplication(final Map<Expression, Set<String>> precondMP) {
		final Map<Expression, Set<String>> mergedPreconditions = new HashMap<Expression, Set<String>>(precondMP);
		final Set<Expression> preconds = new HashSet<Expression>(mergedPreconditions.keySet());

		for (final Expression strongPrecond : preconds) {
			for (final Expression weakPrecond : preconds) {
				if (strongPrecond.getKind() == ExpressionKind.EQ &&
						(weakPrecond.getKind() == ExpressionKind.LTEQ || weakPrecond.getKind() == ExpressionKind.GTEQ)) {
					if (strongPrecond.getExpressions(0).equals(weakPrecond.getExpressions(0)) &&
							strongPrecond.getExpressions(1).equals(weakPrecond.getExpressions(1)))
						if (mergedPreconditions.get(strongPrecond).size() <= mergedPreconditions.get(weakPrecond).size())
							mergedPreconditions.get(weakPrecond).addAll(mergedPreconditions.get(strongPrecond));
				}
				else if (strongPrecond.getKind() == ExpressionKind.LT && weakPrecond.getKind() == ExpressionKind.LTEQ ||
						strongPrecond.getKind() == ExpressionKind.GT && weakPrecond.getKind() == ExpressionKind.GTEQ) {
					if (strongPrecond.getExpressions(0).equals(weakPrecond.getExpressions(0)) &&
							strongPrecond.getExpressions(1).equals(weakPrecond.getExpressions(1)))
						if (mergedPreconditions.get(strongPrecond).size() <= mergedPreconditions.get(weakPrecond).size()) {
							mergedPreconditions.get(weakPrecond).addAll(mergedPreconditions.get(strongPrecond));
							mergedPreconditions.get(strongPrecond).clear();
						}
				}

			}
		}

		return  mergedPreconditions;
	}

	/**
	 * Removes specific preconditions like equality which occur too frequently
	 *
	 * @param precondMP map of preconditions after inference step
	 * @return map of preconditons with specific preconditions removed
	 */
	private Map<Expression, Set<String>> removeEquality(final Map<Expression, Set<String>> precondMP) {
		final Map<Expression, Set<String>> filtPreconditions = new HashMap<Expression, Set<String>>(precondMP);
		final Set<Expression> preconds = new HashSet<Expression>(filtPreconditions.keySet());

		for (final Expression precond : preconds) {
			if (precond.getKind() == ExpressionKind.OTHER) {
				filtPreconditions.remove(precond);
			} else if (precond.getKind() == ExpressionKind.EQ || precond.getKind() == ExpressionKind.NEQ) {
				try {
					if (isIntLit(precond.getExpressions(1)) || isFloatLit(precond.getExpressions(1)))
						filtPreconditions.remove(precond);
					else if (precond.getExpressions(1).getKind() == ExpressionKind.OP_SUB) {
						if (isIntLit(precond.getExpressions(1).getExpressions(0)) ||
								isFloatLit(precond.getExpressions(1).getExpressions(0)))
							filtPreconditions.remove(precond);
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}

		return filtPreconditions;
	}

	/**
	 * Filters out the preconditions with confidence less than sigma
	 *
	 * @return filtered preconditons map
	 */
	private Map<String, Double> doFiltering() {
		final Map<String, Double> precondConfM = calcConfidence(precondMethods);
		precondMethods.clear();

		final Map<String, Double> precondConfP = calcConfidence(precondProjects);
		precondProjects.clear();

		final Map<String, Double> filteredPreconds = new HashMap<String, Double>();

		final Set<String> preconds = precondConfM.keySet();
		for (final String precond : preconds)
			if (precondConfM.get(precond) >= sigma && precondConfP.get(precond) >= sigma)
				filteredPreconds.put(precond, precondConfM.get(precond)*precondConfP.get(precond));

		return filteredPreconds;
	}

	/**
	 * Calculates confidence for each precondition in the map
	 *
	 * @param precondMP map of preconditon and set of clientmethods/projects
	 * @return map of precondition and confidence
	 */
	private Map<String, Double> calcConfidence(final Map<Expression, Set<String>> precondMP) {
		final Map<String, Double> precondConf = new HashMap<String, Double>();
		final Set<Expression> preconds = precondMP.keySet();

		final Set<String> totalCalls = new HashSet<String>();
		for (final Expression precond : preconds)
			if (precondMP.get(precond).size() > 1)
				totalCalls.addAll(precondMP.get(precond));

		for (final Expression precond : preconds) {
			Double conf = 0.0;
			if (precondMP.get(precond).size() > 1)
				conf = precondMP.get(precond).size() / (totalCalls.size() * 1.0);
			precondConf.put(prettyprint(precond), conf);
		}

		return precondConf;
	}

	/**
	 * Ranks the preconditions based on their confidence value
	 *
	 * @param filteredPreconds map of filtered precondtion
	 * @return ranked preconditions
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private List<Map.Entry<String, Double>> doRanking(final Map<String, Double> filteredPreconds) throws IOException, InterruptedException {
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
		comb.add(new TreeSet<String>(Collections.singletonList("rcv$")));
		argList.add("rcv$");

		for (int i = 0; i < args; i++) {
			comb.add(new TreeSet<String>(Collections.singletonList("arg$" + Integer.toString(i))));
			argList.add("arg$" + Integer.toString(i));
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
			return (p2.getValue()).compareTo(p1.getValue());
		}
	}

}
