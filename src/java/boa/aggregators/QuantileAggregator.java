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
import java.util.Arrays;
import java.util.List;

import boa.io.EmitKey;
import boa.output.Output.Value;

/**
 * A Boa aggregator to calculate the quantiles for the values in a dataset.
 *
 * @author anthonyu
 * @author rdyer
 */
abstract class QuantileAggregator extends Aggregator {
	private int total;

	/**
	 * Construct a QuantileAggregator.
	 *
	 * @param n
	 *            A long representing the number of quantiles to calculate
	 */
	public QuantileAggregator(final long n) {
		super(n);
	}

	/**
	 * Parse a string as long and add it to the running total.
	 *
	 * @param metadata
	 *            A {@link Value} containing the number of values, or null
	 *
	 * @return A long representing the value in metadata
	 */
	public long count(final Value metadata) {
		final long count;

		if (metadata == null)
			count = 1;
		else if (metadata.getType() == Value.Type.INT)
			count = metadata.getI();
		else
			count = Double.valueOf(metadata.getF()).longValue();

		this.total += count;

		return count;
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.total = 0;
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		// if we're in the combiner, just output the compressed data
		if (this.isCombining()) {
			for (final Pair<Value, Long> e : this.getTuples())
				this.collect(e.getFirst(), EmitKey.toValue(e.getSecond()));
		} else {
			// otherwise, set up the quantiles
			final int n = (int) (this.getArg() - 1);
			final Value[] quantiles = new Value[n];
			final double step = this.total / (double) n;

			long last = 0;
			long q = 0;
			for (final Pair<Value, Long> e : this.getTuples()) {
				q += e.getSecond();

				final int curr = (int) (q / step);

				if (curr == last)
					continue;

				last = curr;

				quantiles[curr - 1] = e.getFirst();
			}

			this.collect(Arrays.toString(quantiles));
		}
	}

	/**
	 * Return the data points from the dataset in pairs.
	 *
	 * @return A {@link List} of {@link Pair}&lt{@link Value}, {@link Long}&gt;
	 *         containing the data points from the dataset
	 */
	public abstract List<Pair<Value, Long>> getTuples();
}
