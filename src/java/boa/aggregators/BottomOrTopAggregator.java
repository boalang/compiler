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

import java.io.IOException;
import java.util.Map.Entry;

import boa.io.EmitKey;

/**
 * A Boa aggregator to estimate the bottom or top <i>n</i> values in a dataset by
 * cardinality.
 * 
 * @author anthonyu
 * @author rdyer
 */
public abstract class BottomOrTopAggregator extends Aggregator {
	protected CountingSet<String> set;
	protected final CountedString[] list;
	protected final int last;
	protected long DefaultValue;

	/**
	 * Construct a {@link BottomOrTopAggregator}.
	 * 
	 * @param n A long representing the number of values to return
	 */
	public BottomOrTopAggregator(final long n) {
		super(n);

		// an array of weighted string of length n
		this.list = new CountedString[(int) n];
		// the index of the last entry in the list
		this.last = (int) (n - 1);
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.set = new CountingSet<String>();

		// clear out the list
		for (int i = 0; i < this.getArg(); i++)
			this.list[i] = new CountedString(null, DefaultValue);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) {
		if (metadata == null)
			this.set.add(data, 1);
		else
			this.set.add(data, Double.valueOf(metadata).longValue());
	}

	protected abstract boolean shouldInsert(final long a, final long b);

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		if (this.isCombining()) {
			for (final Entry<String, Long> e : this.set.getEntries())
				this.collect(e.getKey().toString(), e.getValue().toString());
		} else {
			// TODO: replace this with the algorithm described in M. Charikar,
			// K. Chen, and M. Farach-Colton, Finding frequent items in data
			// streams, Proc 29th Intl. Colloq. on Automata, Languages and
			// Programming, 2002.

			for (final Entry<String, Long> e : this.set.getEntries())
				if (shouldInsert(e.getValue(), this.list[this.last].getCount()) ||
						(e.getValue() == this.list[this.last].getCount() && this.list[this.last].getString().compareTo(e.getKey()) > 0))
					// find this new item's position within the list
					for (int i = 0; i < this.getArg(); i++)
						if (shouldInsert(e.getValue(), this.list[i].getCount()) ||
								(e.getValue() == this.list[i].getCount() && this.list[i].getString().compareTo(e.getKey()) > 0)) {
							// here it is. move all subsequent items down one
							for (int j = (int) (this.getArg() - 2); j >= i; j--)
								this.list[j + 1] = this.list[j];

							// insert the item where it belongs
							this.list[i] = new CountedString(e.getKey(), e.getValue());
							break;
						}

			for (final CountedString c : this.list)
				if (c.getString() != null)
					this.collect(c.toString());
		}
	}
}

/**
 * A tuple containing a {@link String} and its count.
 * 
 * @author anthonyu
 */
class CountedString {
	private final String string;
	private final long count;

	/**
	 * Construct a {@link CountedString}.
	 * 
	 * @param string A {@link String} containing the string part of the tuple
	 * @param weight A long representing the count part of the tuple
	 */
	public CountedString(final String string, final long count) {
		this.string = string;
		this.count = count;
	}

	/**
	 * Get the string part of the tuple.
	 * 
	 * @return A {@link String} containing the string part of the tuple
	 */
	public String getString() {
		return this.string;
	}

	/**
	 * Get the string part of the tuple.
	 * 
	 * @return A long representing the count part of the tuple
	 */
	public long getCount() {
		return this.count;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.string + ", " + this.count;
	}
}
