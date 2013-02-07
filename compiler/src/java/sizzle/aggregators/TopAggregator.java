package sizzle.aggregators;

/**
 * A Sizzle aggregator to estimate the top <i>n</i> values in a dataset by
 * cardinality.
 * 
 * @author anthonyu
 * @author rdyer
 */
@AggregatorSpec(name = "top", formalParameters = { "int" }, weightType = "int")
public class TopAggregator extends BottomOrTopAggregator {
	/**
	 * Construct a {@link TopAggregator}.
	 * 
	 * @param n A long representing the number of values to return
	 */
	public TopAggregator(final long n) {
		super(n);

		DefaultValue = Long.MIN_VALUE;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean shouldInsert(final long a, final long b) {
		return a > b;
	}
}
