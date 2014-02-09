package boa.aggregators;

import java.io.IOException;

import boa.io.EmitKey;

/**
 * A Boa aggregator to calculate a mean of the values in a dataset.
 * 
 * @author anthonyu
 */
@AggregatorSpec(name = "mean", type = "float", canCombine = true)
public class FloatMeanAggregator extends MeanAggregator {
	private double sum;

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.sum = 0;
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException {
		this.aggregate(Double.parseDouble(data), metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final long data, final String metadata) {
		this.aggregate(Long.valueOf(data).doubleValue(), metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final double data, final String metadata) {
		this.sum += data;

		super.count(metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		// if we are in the combiner, output the sum and the count
		if (this.isCombining())
			this.collect(this.sum, Long.toString(this.getCount()));
		// otherwise, output the final answer
		else
			this.collect(this.sum / this.getCount());
	}
}
