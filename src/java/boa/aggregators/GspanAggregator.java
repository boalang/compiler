package boa.aggregators;

import java.io.IOException;
import java.util.HashMap;

import boa.io.EmitKey;

import boa.aggregators.GspanSearcher;

import boa.graphs.cfg.CFG;

/**
 * A Boa aggregator to calculate the sum of the values in a dataset.
 * 
 * @author anthonyu
 * @author rdyer
 */
@AggregatorSpec(name = "gSpan", type = "CFG", canCombine = true)
public class GspanAggregator extends Aggregator {

	private GspanSearcher<CFG> gss; //TODO: make work with all graph types.
	private HashMap<String, Integer> results;
	
	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);
		
		gss = new GspanSearcher<CFG>(3); //TODO: figure out a good size.
		results = new HashMap<String, Integer>();
	}
	
	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException, FinishedException {
		System.out.println("in aggregate");
		//TODO: last step of aggregation - combine HashMaps
	}

	public void aggregate(final CFG data, final String metadata) throws IOException, FinishedException {
		//First time through we aggregates CFGs, second time through it is HMs
		HashMap<String, Integer> my_patterns = gss.search(data);
		
		results.putAll(my_patterns);
	}
	
	public void aggregate(final CFG data) throws IOException, FinishedException {
		this.aggregate(data, null);
	}
	
	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		this.collect(this.results.toString());
	}

}
