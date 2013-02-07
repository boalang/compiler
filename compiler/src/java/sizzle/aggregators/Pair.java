package sizzle.aggregators;

/**
 * A pair of values.
 * 
 * @author anthonyu
 * 
 * @param <F> The type of the first value
 * @param <S> The type of the second value
 */
class Pair<F, S> {
	private final F first;
	private final S second;

	/**
	 * Construct a {@link Pair}.
	 * 
	 * @param first The first value
	 * @param second The second value
	 */
	public Pair(final F first, final S second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Get the first value.
	 * 
	 * @return The first value
	 */
	public F getFirst() {
		return this.first;
	}

	/**
	 * Get the second value.
	 * 
	 * @return The second value
	 */
	public S getSecond() {
		return this.second;
	}
}
