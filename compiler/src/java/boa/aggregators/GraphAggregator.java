package boa.aggregators;

import java.util.*;
import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import boa.io.EmitKey;
import boa.io.EmitValue;


/**
 * The base class for all graph output aggregators.
 * 
 * @author rdyer
 */
public abstract class GraphAggregator extends Aggregator {
	protected Set<String> neighbors;
	protected Map<String,String> weights;

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.neighbors = new HashSet<String>();
		this.weights = new HashMap<String,String>();
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException, FinishedException {
		final String neighbor = data.startsWith("\"") ? data : "\"" + data + "\"";
		this.neighbors.add(neighbor);
		final String weight = metadata == null ? null : (metadata.startsWith("\"") ? metadata : "\"" + metadata + "\"");
		this.weights.put(neighbor, weight);
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	protected void collect(final String data, final String metadata) throws IOException, InterruptedException {
		if (this.isCombining()) {
			this.getContext().write(this.getKey(), new EmitValue(data, metadata));
			return;
		}
		this.getContext().write(new Text(format(this.getKey().getIndex(), data, metadata)), NullWritable.get());
	}

	protected abstract String format(final String idx, final String data, final String metadata);

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
