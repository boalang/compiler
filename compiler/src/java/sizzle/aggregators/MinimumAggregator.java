package sizzle.aggregators;

/**
 * A Sizzle aggregator to calculate the bottom <i>n</i> values in a dataset by
 * weight.
 * 
 * @author anthonyu
 */
@AggregatorSpec(name = "minimum", formalParameters = { "int" }, weightType = "float")
public class MinimumAggregator extends MinOrMaxAggregator {
	/**
	 * Construct a {@link MinimumAggregator}.
	 * 
	 * @param n A long representing the number of values to return
	 */
	public MinimumAggregator(final long n) {
		super(n);

		DefaultWeight = Double.MAX_VALUE;
	}

	/** {@inheritDoc} */
	@Override
	public int compare(final WeightedString a, final WeightedString b) {
		final double delta = b.getWeight() - a.getWeight();

		// if the weights are different, return the difference
		if (Math.abs(delta) > 0)
			return (int) Math.ceil(delta);

		// otherwise compare the strings
		return b.getString().compareTo(a.getString());
	}
}
