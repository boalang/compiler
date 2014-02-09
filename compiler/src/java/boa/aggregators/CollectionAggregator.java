package boa.aggregators;

import java.io.IOException;

/**
 * A Boa aggregator to output all of the values in a dataset.
 * 
 * @author anthonyu
 * @author rdyer
 */
@AggregatorSpec(name = "collection")
public class CollectionAggregator extends Aggregator {
	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException {
		// just pass it through
		this.collect(data);
	}
}
