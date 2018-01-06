package boa.aggregators;

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

}
