package boa.aggregators;

import java.io.IOException;
import java.util.HashMap;

import boa.functions.BoaCasts;
import boa.io.EmitKey;

/**
 * A Boa aggregator to count through subpatterns.
 * 
 * @author DavidMOBrien
 * @author rdyer
 */
@AggregatorSpec(name = "gSpanAgg", formalParameters = { "double" }, canCombine = true)
public class GspanAggregator extends MeanAggregator {

	private HashMap<String, Integer> results;
	private double freq;
	
	public GspanAggregator(final double n) {
		this.freq = n;
	}
	
	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);
		
		results = new HashMap<String, Integer>();
	}
	
	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException {
	}
	
	/** {@inheritDoc} */
	@Override
	public void aggregate(final HashMap<String, Integer> data, final String metadata) throws IOException, InterruptedException, FinishedException {
		
		this.count(metadata);
		
		for (String key : data.keySet()) {
			if (results.containsKey(key)) {
				results.put(key, results.get(key) + data.get(key));
			} else {
				results.put(key, data.get(key));
			}
		}
	}
	
	public HashMap<String, Integer> filter() {
		HashMap<String, Integer> temp = new HashMap<String, Integer>();
		
		double minimum = this.getCount() * this.freq; //TODO: change from hard-coded.
		
		for (String key: this.results.keySet()) {
			if (this.results.get(key) > minimum) {
				temp.put(key, this.results.get(key));
			}
		}
		
		return temp;
	}
	
	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		// if we are in the combiner, output the sum and the count
			if (this.isCombining())
				this.collect(this.results, BoaCasts.longToString(this.getCount()));
			// otherwise, output the final answer
			else {
				this.collect(filter().toString());
			}
	}

}
