package boa.aggregators;

import java.io.IOException;

import boa.io.EmitKey;
import boa.io.EmitValue;

/**
 * A Boa aggregator to calculate the sum of the values in a dataset.
 * 
 * @author anthonyu
 */
@AggregatorSpec(name = "sum", type = "int")
public class IntSumAggregator extends Aggregator {
	private long sum;

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.sum = 0;
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException, FinishedException {
		this.aggregate(Double.valueOf(data).longValue());
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final long data, final String metadata) {
		this.sum += data;
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final double data, final String metadata) throws IOException, InterruptedException, FinishedException {
		this.aggregate(Double.valueOf(data).longValue());
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		this.collect(this.sum);
	}

	/** {@inheritDoc} */
	@Override
	public EmitValue getResult() {
		return new EmitValue(this.sum);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssociative() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCommutative() {
		return true;
	}
}
