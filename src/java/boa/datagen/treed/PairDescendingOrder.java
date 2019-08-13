package boa.datagen.treed;

import java.util.Comparator;

public class PairDescendingOrder implements Comparator<Pair>
{
	/**
	 * Sort in reverse natural order.
	 * Defines an alternate sort order for Pair.
	 * Compare two Pair Objects.
	 * Compares descending.
	 *
	 * @param p1 first String to compare
	 * @param p2 second String to compare
	 *
	 * @return +1 if p1 &lt; p2, 0 if p1 == p2, -1 if p1 &gt; p2
	 */
	@Override
	public final int compare(Pair p1, Pair p2)
	{
		return p2.compareTo(p1);
	}
}
