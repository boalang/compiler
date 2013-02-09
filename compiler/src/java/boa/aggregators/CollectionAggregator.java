package boa.aggregators;

import java.io.IOException;

import boa.io.EmitValue;


/**
 * A Boa aggregator to output all of the values in a dataset.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "collection")
public class CollectionAggregator extends Aggregator {
	private String data;

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException {
		this.data = data;
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		this.collect(this.data);
	}

	/** {@inheritDoc} */
	@Override
	public EmitValue getResult() {
		return new EmitValue(this.data);
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
