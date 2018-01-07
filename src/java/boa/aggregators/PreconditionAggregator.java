package boa.aggregators;

import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Expression;
import boa.io.EmitKey;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        precondMethods = doInference(precondMethods);
        precondProjects = doInference(precondProjects);

        //doFiltering(inferredPreconditions);
        //doRanking();

        for (Expression precond: precondMethods.keySet()) {
            this.collect(prettyprint(precond) + " : " + precondMethods.get(precond).size() );
        }
    }

    /**
     * Infer the weak preconditions from the mined preconditions.
     *
     * @return map
     */
    private Map<Expression, Set<String>> doInference(Map<Expression, Set<String>> precondMP) {
        Map<Expression, Set<String>> infPreconditions = new HashMap<Expression, Set<String>>(precondMP);
        Set<Expression> preconds = new HashSet<Expression>(infPreconditions.keySet());
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

    private void doFiltering(Map<String, Map<Expression, Set<String>>> infPreconditions) {
        
    }

    private void doRanking() {
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