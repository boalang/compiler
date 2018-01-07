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
    private Map<String, Map<Expression, Set<String>>> apiPrecondition;

    /**
     * Construct a {@link PreconditionAggregator}
     */
    public PreconditionAggregator() {
        this.apiPrecondition = new HashMap<String, Map<Expression, Set<String>>>();
    }

    /** {@inheritDoc} */
    @Override
    public void aggregate(String data, String metadata) throws IOException, InterruptedException, FinishedException {
        //data expected format: "pid:client:fq_clientmethodname"
        EmitKey key = getKey();
        String api = key.getIndex();

        int splitLoc = data.indexOf(':');
        String clientmethod = data.substring(0, splitLoc);
        String precond = data.substring(splitLoc+1, data.length());

        Expression precondition = parseexpression(precond);

        if (apiPrecondition.containsKey(api)) {
            if (apiPrecondition.get(api).containsKey(precondition)) {
                apiPrecondition.get(api).get(precondition).add(clientmethod);
            }
            else {
                Set<String>  setOfClients = new HashSet<String>();
                setOfClients.add(clientmethod);
                apiPrecondition.get(api).put(precondition, setOfClients);
            }
        } else {
            Set<String>  setOfClients = new HashSet<String>();
            setOfClients.add(clientmethod);
            Map<Expression, Set<String>> precondClients = new HashMap<Expression, Set<String>>();
            precondClients.put(precondition, setOfClients);
            apiPrecondition.put(api, precondClients);
        }

    }

    /** {@inheritDoc} */
    @Override
    public void finish() throws IOException, InterruptedException {
        Map<String, Map<Expression, Set<String>>> inferredPreconditions = doInference();
        apiPrecondition.clear();
        //doFiltering();
        //doRanking();

        for (String api: inferredPreconditions.keySet()) {
            Set<Expression> preconds = inferredPreconditions.get(api).keySet();
            for (Expression precond: preconds) {
                this.collect(prettyprint(precond) + " : " + inferredPreconditions.get(api).get(precond).size() );
            }
        }
    }

    /**
     * Infer the weak preconditions from the mined preconditions.
     *
     * @return map
     */
    private Map<String, Map<Expression, Set<String>>> doInference() {
        Map<String, Map<Expression, Set<String>>> infPrecondition = new HashMap<String, Map<Expression, Set<String>>>();
        Set<String> apis = apiPrecondition.keySet();

        for (String api: apis) {
            Map<Expression, Set<String>> precondClients = new HashMap<Expression, Set<String>>(apiPrecondition.get(api));
            Set<Expression> preconds = new HashSet<Expression>(precondClients.keySet());
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
                                        precondClients.put(nsineqPrecond, new HashSet<String>());

                                } else {
                                    nsineqPrecond = parseexpression(prettyprint(lhs) + "<=" + prettyprint(rhs));

                                    if (!containsKind(preconds, ExpressionKind.LTEQ))
                                        precondClients.put(nsineqPrecond, new HashSet<String>());
                                }

                                if (precondClients.get(eqPrecond).size() == precondClients.get(sineqPrecond).size())
                                    precondClients.put(nsineqPrecond, union(precondClients.get(nsineqPrecond),
                                            union(precondClients.get(eqPrecond),
                                                    precondClients.get(sineqPrecond))));
                                else if (precondClients.get(eqPrecond).size() > precondClients.get(sineqPrecond).size())
                                    precondClients.put(nsineqPrecond, union(precondClients.get(nsineqPrecond),
                                            precondClients.get(sineqPrecond)));
                                else
                                    precondClients.put(nsineqPrecond, union(precondClients.get(nsineqPrecond),
                                            precondClients.get(eqPrecond)));

                                //Conditions with implications
                                if (precondClients.get(eqPrecond).size() <= precondClients.get(nsineqPrecond).size())
                                    count2++;

                                if (precondClients.get(sineqPrecond).size() <= precondClients.get(nsineqPrecond).size())
                                    precondClients.remove(sineqPrecond);
                            }
                        }
                    }

                    if (count2 == 2 || (count2 == 1 && count1 == 1))
                        precondClients.remove(eqPrecond);
                }
            }

            infPrecondition.put(api, precondClients);
        }

        return infPrecondition;
    }

    private void doFiltering() {
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