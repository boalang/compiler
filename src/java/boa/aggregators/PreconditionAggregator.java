/*
 * Copyright 2017, Robert Dyer, Mohd Arafat
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


@AggregatorSpec(name = "precondition")
public class PreconditionAggregator extends Aggregator {
    private Map<Expression, Set<String>> precondMethods;
    private Map<Expression, Set<String>> precondProjects;

    /**
     * Construct a {@link PreconditionAggregator}
     */
    public PreconditionAggregator() {
        this.precondMethods = new HashMap<Expression, Set<String>>();  //preconditions: set of methods
        this.precondProjects = new HashMap<Expression, Set<String>>(); //preconditions: set of projects
    }

    /** {@inheritDoc} */
    @Override
    public void aggregate(String data, String metadata) throws IOException, InterruptedException, FinishedException {
        //data expected format: "pid:client:fq_clientmethodname"

        int splitIndex = data.indexOf(':');
        String project = data.substring(0, splitIndex);
        String clientmethod = data.substring(0, data.indexOf(':', splitIndex+1));
        String precond = data.substring(data.indexOf(':', splitIndex+1)+1, data.length());

        Expression precondition = parseexpression(precond);

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
        Map<String, Double> filteredPreconds = doFiltering();
        SortedMap<String, Double> rankedPreconds = doRanking(filteredPreconds);

        //Send to Writer
        for (String precond: rankedPreconds.keySet()) {
            this.collect(precond + " : " + rankedPreconds.get(precond));
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
        Map<Expression, Set<String>> infPreconditions = new HashMap<Expression, Set<String>>(precondMP);
        Set<Expression> preconds = infPreconditions.keySet();
        int count1 = 0;
        int count2 = 0;

        for (Expression eqPrecond: preconds) {
            if (eqPrecond.getKind() == ExpressionKind.EQ) {

                for (Expression sineqPrecond: preconds) {
                    if (sineqPrecond.getKind() == ExpressionKind.LT || sineqPrecond.getKind() == ExpressionKind.GT) {
                        if (eqPrecond.getExpressions(0).equals(sineqPrecond.getExpressions(0)) &&
                                    eqPrecond.getExpressions(1).equals(sineqPrecond.getExpressions(1))) {

                            count1++;
                            Expression nsineqPrecond;
                            Expression lhs = sineqPrecond.getExpressions(0);
                            Expression rhs = sineqPrecond.getExpressions(1);

                            if (sineqPrecond.getKind() == ExpressionKind.GT) {
                                nsineqPrecond = parseexpression(prettyprint(lhs) + ">=" + prettyprint(rhs));

                                if (!containsKind(preconds, ExpressionKind.GTEQ))
                                    infPreconditions.put(nsineqPrecond, new HashSet<String>());

                            } else {
                                nsineqPrecond = parseexpression(prettyprint(lhs) + "<=" + prettyprint(rhs));

                                if (!containsKind(preconds, ExpressionKind.LTEQ))
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
                            if (infPreconditions.get(eqPrecond).size() <= infPreconditions.get(nsineqPrecond).size())
                                count2++;

                            if (infPreconditions.get(sineqPrecond).size() <= infPreconditions.get(nsineqPrecond).size())
                                infPreconditions.get(sineqPrecond).clear();
                        }
                    }
                }

                if (count2 == 2 || (count2 == 1 && count1 == 1))
                    infPreconditions.get(eqPrecond).clear();  //not removing for consistency b/w methods and projects
            }
        }

        return infPreconditions;
    }

    /**
     * Filter out the preconditions with confidence less than 0.5
     *
     * @return filtered preconditons map
     */

    private Map<String, Double> doFiltering() {
        Map<String, Double> precondConfM = calConfidence(precondMethods);
        precondMethods = new WeakHashMap<Expression, Set<String>>(0); //Reclaim memory
        Map<String, Double> precondConfP = calConfidence(precondProjects);
        precondProjects = new WeakHashMap<Expression, Set<String>>(0);

        Map<String, Double> filteredPreconds = new HashMap<String, Double>();

        Set<String> preconds= precondConfM.keySet();
        for (String precond: preconds) {
            if (precondConfM.get(precond) >= 0.5 && precondConfP.get(precond) >= 0.5)
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
    private Map<String, Double> calConfidence(Map<Expression, Set<String>> precondMP) {
        Map<String, Double> precondConf = new HashMap<String, Double>();
        Set<Expression> preconds = precondMP.keySet();

        Set<String> totalCalls = new HashSet<String>();
        for (Expression precond: preconds)
            totalCalls.addAll(precondMP.get(precond));

        for (Expression precond: preconds) {
            Double conf = precondMP.get(precond).size()/(totalCalls.size()*1.0);
            precondConf.put(prettyprint(precond), conf);
        }

        return precondConf;
    }

    /**
     * Rank the preconditions based on their confidence value
     *
     * @param filteredPreconds map of filtered precondtion
     * @return ranked preconditions
     */
    private SortedMap<String, Double> doRanking(Map<String, Double> filteredPreconds) {
        SortedMap<String, Double> rankedPreconds = new TreeMap<String, Double>(new PreconditionComparator(filteredPreconds));
        rankedPreconds.putAll(filteredPreconds);
        return rankedPreconds;
    }

    /**
     * Comparator to sort map based on values
     *
     */
    public class PreconditionComparator implements Comparator<String> {
        private Map<String, Double>  m;

        public PreconditionComparator(Map<String, Double> m) {
            this.m = m;
        }

        public int compare(String a, String b) {
            return (m.get(b) + b).compareTo(m.get(a) + a);
        }
    }

    /**
     * Checks the presence of a particular ExpressionKind in a set.
     *
     * @param exprs set of expressions
     * @param eKind ExpressionKind to be searched
     * @return true if Expressionkind is present in the set
     */
    private boolean containsKind(Set<Expression> exprs, ExpressionKind eKind) {
        for (Expression e: exprs) {
            if (eKind == e.getKind())
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
    private <T> Set<T> union(Set<T> s1, Set<T> s2) {
        Set<T> s = new HashSet<T>(s1);
        s.addAll(s2);
        return s;
    }

}