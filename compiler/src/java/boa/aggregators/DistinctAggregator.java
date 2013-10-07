package boa.aggregators;

import java.io.IOException;

import org.apache.hadoop.util.bloom.DynamicBloomFilter;
import org.apache.hadoop.util.bloom.Filter;
import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;

import boa.io.EmitKey;

/**
 * A Boa aggregator to estimate the set of the unique values in a dataset.
 * Roughly equivalent to a distinct(*).
 * 
 * @author anthonyu
 */
@AggregatorSpec(name = "distinct")
public class DistinctAggregator extends Aggregator {
	// from o.a.h.io.BloomMapFile#initBloomFilter
	private static final int HASH_COUNT = 5;

	private final int vectorSize;
	// Our desired error rate is by default 0.005, i.e. 0.5%
	private static final float errorRate = 0.005f;

	protected Filter filter;

	/**
	 * Construct a DistinctAggregator.
	 * 
	 * @param arg
	 *            The size of the internal table used to perform the
	 *            calculation.
	 */
	public DistinctAggregator(final long arg) {
		super(arg);

		// this is all cribbed from o.a.h.io.BloomMapFile#initBloomFilter

		// vector size should be <code>-kn / (ln(1 - c^(1/k)))</code> bits for
		// single key, where <code>k<code> is the number of hash functions,
		// <code>n</code> is the number of keys and <code>c</code> is the
		// desired max. error rate.
		this.vectorSize = (int) Math.ceil(-HASH_COUNT * arg / Math.log(1.0 - Math.pow(errorRate, 1.0 / HASH_COUNT)));
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.filter = new DynamicBloomFilter(this.vectorSize, HASH_COUNT, Hash.MURMUR_HASH, (int) this.getArg());
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException {
		// instantiate a bloom filter input key initialized by the data
		Key key = new Key(data.getBytes());

		// if the key is already in the filter, forget it
		if (this.filter.membershipTest(key))
			return;

		// add the key to the bloom filter
		this.filter.add(key);

		// and collect it
		this.collect(data);
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
