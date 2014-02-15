package boa.aggregators;

/**
 * A Boa aggregator to calculate the top <i>n</i> values in a dataset by
 * weight.
 * 
 * @author anthonyu
 */
@AggregatorSpec(name = "maximum", formalParameters = { "int" }, weightType = "float", canCombine = true)
public class MaximumAggregator extends MinOrMaxAggregator {
	/**
	 * Construct a {@link MaximumAggregator}.
	 * 
	 * @param n A long representing the number of values to return
	 */
	public MaximumAggregator(final long n) {
		super(n);

		DefaultWeight = Double.MIN_VALUE;
	}

	/** {@inheritDoc} */
	@Override
	public int compare(final WeightedString a, final WeightedString b) {
		final double delta = a.getWeight() - b.getWeight();

		// if the weights are different, return the difference
		if (Math.abs(delta) > 0)
			return (int) Math.ceil(delta);

		// otherwise compare the strings
		return b.getString().compareTo(a.getString());
	}
}
