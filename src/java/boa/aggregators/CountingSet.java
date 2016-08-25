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
	private Map<T, Double> map;

	/**
	 * Construct a {@link CountingSet}.
	 */
	public CountingSet() {
		this.map = new HashMap<T, Double>();
	}

	/**
	 * Add a value and its cardinality to the set.
	 * 
	 * @param t The value to be added
	 * @param n The cardinality of the value
	 */
	public void add(final T t, final double n) {
		// if the map already has this key, add n to the current cardiality and reinsert
		if (this.map.containsKey(t))
			this.map.put(t, this.map.get(t) + n);
		else
			this.map.put(t, n);
	}

	public void clear() {
		this.map.clear();
	}

	/**
	 * Get the entries in this set.
	 * 
	 * @return A {@link Set} of T containing the entries in this set
	 */
	public Set<Entry<T, Double>> getEntries() {
		return this.map.entrySet();
	}
}
