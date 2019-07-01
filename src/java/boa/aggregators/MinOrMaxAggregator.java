/*
 * Copyright 2019, Anthony Urso, Hridesh Rajan, Robert Dyer,
 *                 Bowling Green State University
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

import boa.functions.BoaCasts;
import boa.io.EmitKey;
import boa.output.Output.Value;

/**
 * A Boa aggregator to calculate the top or bottom <i>n</i> values in a
 * dataset by weight.
 *
 * @author anthonyu
 * @author rdyer
 */
abstract class MinOrMaxAggregator extends Aggregator {
	protected final WeightedValue[] list;
	private final int last;
	protected double DefaultWeight;

	/**
	 * Construct a {@link MinOrMaxAggregator}.
	 *
	 * @param n
	 *            A long representing the number of values to return
	 */
	public MinOrMaxAggregator(final long n) {
		super(n);

		// an array of weighted string of length n
		this.list = new WeightedValue[(int) this.getArg()];

		// the index of the last entry in the list
		this.last = (int) (this.getArg() - 1);
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		// clear out the list
		for (int i = 0; i < this.getArg(); i++)
			this.list[i] = new WeightedValue(EmitKey.toValue(""), DefaultWeight);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final Value metadata) {
		double weight;

		if (metadata == null)
			weight = 1.0;
		else
			weight = metadata.getF();

		final WeightedValue s = new WeightedValue(EmitKey.toValue(data), weight);

		if (this.compare(s, this.list[this.last]) > 0)
			// find this new item's position within the list
			for (int i = 0; i < this.getArg(); i++)
				if (this.compare(s, this.list[i]) > 0) {
					// we found it. move all subsequent items down one spot
					for (int j = (int) (this.getArg() - 2); j >= i; j--)
						this.list[j + 1] = this.list[j];

					// insert the item where it belongs
					this.list[i] = s;

					break;
				}
	}

	/**
	 * Compare two weighted strings.
	 *
	 * @param a
	 *            A {@link WeightedValue} containing a {@link String} and its
	 *            weight.
	 *
	 * @param b
	 *            A {@link WeightedValue} containing a {@link String} and its
	 *            weight.
	 *
	 * @return A positive integer if <em>a</em> is smaller than <em>b</em>, zero
	 *         if they are equal, and a negative integer if <em>a</em> is larger
	 *         than <em>b</em>.
	 */
	abstract protected int compare(WeightedValue a, WeightedValue b);

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		for (int i = 0; i < this.getArg(); i++)
			this.collect(this.list[i].getValue(), EmitKey.toValue(this.list[i].getWeight()));
	}
}
