/*
 * Copyright 2015, Anthony Urso, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology,
 *                 and Bowling Green State University
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
	protected final CountingSet<String> set = new CountingSet<String>();

	protected final WeightedString[] list;
	protected final int last;

	protected double DefaultValue;

	/**
	 * Construct a {@link BottomOrTopAggregator}.
	 * 
	 * @param n A long representing the number of values to return
	 */
	public BottomOrTopAggregator(final long n) {
		super(n);

		// an array of weighted string of length n
		this.list = new WeightedString[(int) n];
		// the index of the last entry in the list
		this.last = (int) (n - 1);
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		// clear out the data
		this.set.clear();

		final WeightedString defaultItem = new WeightedString(null, this.DefaultValue);
		for (int i = 0; i <= this.last; i++)
			this.list[i] = defaultItem;
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) {
		if (metadata == null)
			this.set.add(data, 1.0);
		else
			this.set.add(data, Double.valueOf(metadata));
	}

	protected abstract boolean shouldInsert(final double a, final double b);

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		if (this.isCombining()) {
			for (final Entry<String, Double> e : this.set.getEntries())
				this.collect(e.getKey().toString(), e.getValue().toString());
		} else {
			// TODO: replace this with the algorithm described in M. Charikar,
			// K. Chen, and M. Farach-Colton, Finding frequent items in data
			// streams, Proc 29th Intl. Colloq. on Automata, Languages and
			// Programming, 2002.

			for (final Entry<String, Double> e : this.set.getEntries())
				if (shouldInsert(e.getValue(), this.list[this.last].getWeight()) ||
						(e.getValue() == this.list[this.last].getWeight() && this.list[this.last].getString().compareTo(e.getKey()) > 0))
					// find this new item's position within the list
					for (int i = 0; i <= this.last; i++)
						if (shouldInsert(e.getValue(), this.list[i].getWeight()) ||
								(e.getValue() == this.list[i].getWeight() && this.list[i].getString().compareTo(e.getKey()) > 0)) {
							// here it is. move all subsequent items down one
							for (int j = this.last - 1; j >= i; j--)
								this.list[j + 1] = this.list[j];

							// insert the item where it belongs
							this.list[i] = new WeightedString(e.getKey(), e.getValue());
							break;
						}

			for (final WeightedString c : this.list)
				if (c.getString() != null)
					this.collect(c.toString());
		}
	}
}
