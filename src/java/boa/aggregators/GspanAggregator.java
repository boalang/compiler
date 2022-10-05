package boa.aggregators;

import java.io.IOException;

import boa.io.EmitKey;

/**
 * A Boa aggregator to calculate the sum of the values in a dataset.
 * 
 * @author anthonyu
 * @author rdyer
 */
@AggregatorSpec(name = "gSpan", type = "CFG", canCombine = true)
public class GspanAggregator extends Aggregator {

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException, FinishedException {
		System.out.println(data);
	}

}
