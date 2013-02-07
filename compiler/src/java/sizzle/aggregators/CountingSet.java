package sizzle.aggregators;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A counting set. Like a {@link Set}, but also keeps track of how many times a
 * given member has been added.
 * 
 * @author anthonyu
 * 
 * @param <T> The type of value that will be inserted into the set
 */
class CountingSet<T> {
	private Map<T, Long> map;

	/**
	 * Construct a {@link CountingSet}.
	 */
	public CountingSet() {
		this.map = new HashMap<T, Long>();
	}

	/**
	 * Add a value to the set, with cardinality 1.
	 * 
	 * @param t The value to be added
	 */
	public void add(final T t) {
		this.add(t, 1);
	}

	/**
	 * Add a value and its cardinality to the set.
	 * 
	 * @param t The value to be added
	 * @param n The cardinality of the value
	 */
	public void add(final T t, final long n) {
		// if the map already has this key, add n to the current cardiality and reinsert
		if (this.map.containsKey(t))
			this.map.put(t, this.map.get(t) + n);
		else
			this.map.put(t, n);
	}

	public void clear() {
		this.map = new HashMap<T, Long>();
	}

	/**
	 * Get the entries in this set.
	 * 
	 * @return A {@link Set} of T containing the entries in this set
	 */
	public Set<Entry<T, Long>> getEntries() {
		return this.map.entrySet();
	}
}
