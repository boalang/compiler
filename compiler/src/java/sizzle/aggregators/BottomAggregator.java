package sizzle.aggregators;

/**
 * A Sizzle aggregator to estimate the bottom <i>n</i> values in a dataset by
 * cardinality.
 * 
 * @author anthonyu
 * @author rdyer
 */
@AggregatorSpec(name = "bottom", formalParameters = { "int" }, weightType = "int")
public class BottomAggregator extends BottomOrTopAggregator {
	/**
	 * Construct a {@link BottomAggregator}.
	 * 
	 * @param n A long representing the number of values to return
	 */
	public BottomAggregator(final long n) {
		super(n);

		DefaultValue = Long.MAX_VALUE;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean shouldInsert(final long a, final long b) {
		return a < b;
	}
}
