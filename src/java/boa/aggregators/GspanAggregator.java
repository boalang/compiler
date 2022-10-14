package boa.aggregators;

import java.io.IOException;
import java.util.HashMap;

import boa.io.EmitKey;

import boa.functions.BoaCasts;
import boa.graphs.cfg.CFG;

import static boa.functions.BoaGraphIntrinsics.cfgToDot;

/**
 * A Boa aggregator to count through subpatterns.
 * 
 * @author DavidMOBrien
 * @author rdyer
 */
@AggregatorSpec(name = "gSpanAgg", canCombine = true)
public class GspanAggregator extends Aggregator {

	private HashMap<String, Integer> results;
	
	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);
		
		results = new HashMap<String, Integer>();
	}
	
	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException {
		this.collect(data);
	}
	
	/** {@inheritDoc} */
	@Override
	public void aggregate(final HashMap<String, Integer> data, final String metadata) throws IOException, InterruptedException, FinishedException {
		
		for (String key : data.keySet()) {
			if (results.containsKey(key)) {
				results.put(key, results.get(key) + data.get(key));
			} else {
				results.put(key, data.get(key));
			}
		}
	}
	
	public void aggregate(final HashMap<String, Integer> data) throws IOException, InterruptedException, FinishedException {
		this.aggregate(data, null);
	}
	
	private void filter_results() {
		//TODO: change this later
		
		for (String key : this.results.keySet()) {
			if (this.results.get(key) < 10) {
				this.results.remove(key);
			}
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		// if we are in the combiner, output the sum and the count
			if (this.isCombining())
				this.collect(this.results, null);
			// otherwise, output the final answer
			else {
				this.collect(this.results.toString());
			}
	}

}
