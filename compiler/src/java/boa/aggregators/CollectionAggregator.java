package boa.aggregators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import boa.io.EmitKey;


/**
 * A Boa aggregator to output all of the values in a dataset.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "collection")
public class CollectionAggregator extends Aggregator {
	private List<String> data;

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.data = new ArrayList<String>();
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException {
		this.data.add(data);
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		for (final String s : this.data)
			this.collect(s);

		super.finish();
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
