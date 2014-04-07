/*
 * Copyright 2014, Anthony Urso, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.aggregators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * A sorted counting set. Like a {@link SortedSet}, but also keeps track of how many
 * times a given member has been added.
 * 
 * @author anthonyu
 * 
 * @param <T> The type of value that will be inserted into the set
 */
class SortedCountingSet<T> implements Iterable<T> {
	private final TreeMap<T, Long> map;

	/**
	 * Construct a {@link SortedCountingSet}.
	 */
	public SortedCountingSet() {
		this.map = new TreeMap<T, Long>();
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

	/** {@inheritDoc} */
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private Entry<T, Long> lastEntry = SortedCountingSet.this.map.lastEntry();
			private Entry<T, Long> thisEntry = SortedCountingSet.this.map.firstEntry();

			private long cursor = 0;

			@Override
			public boolean hasNext() {
				if (this.lastEntry == null)
					return false;

				if (!this.thisEntry.getKey().equals(this.lastEntry.getKey()))
					return true;

				return this.cursor != this.lastEntry.getValue().longValue();
			}

			@Override
			public T next() {
				if (this.cursor == this.thisEntry.getValue().longValue()) {
					this.thisEntry = SortedCountingSet.this.map.higherEntry(this.thisEntry.getKey());
					this.cursor = 0;
				}

				this.cursor++;

				return this.thisEntry.getKey();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Copy this set into a {@link List}.
	 * 
	 * @return A {@link List} containing the values in this set
	 */
	public List<T> toList() {
		final List<T> l = new ArrayList<T>();

		for (final T t : this)
			l.add(t);

		return l;
	}

	/**
	 * Get the entries in this set.
	 * 
	 * @return A {@link Set} of Map.Entry containing the entries in this set
	 */
	public Set<java.util.Map.Entry<T, Long>> getEntries() {
		return this.map.entrySet();
	}
}
