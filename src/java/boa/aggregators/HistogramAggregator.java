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
import java.util.Arrays;
import java.util.List;

import boa.io.EmitKey;

/**
 * A Boa aggregator to calculate a histogram for the values in a dataset.
 * 
 * @author anthonyu
 */
abstract class HistogramAggregator extends Aggregator {
	private final long min;
	private final long max;
	private final int buckets;

	/**
	 * Construct a HistogramAggregator.
	 * 
	 * @param min
	 *            A long representing the minimum value to be considered in the
	 *            histogram
	 * 
	 * @param max
	 *            A long representing the maximum value to be considered in the
	 *            histogram
	 * 
	 * @param buckets
	 *            A long representing the number of buckets in the histogram
	 */
	public HistogramAggregator(final long min, final long max, final long buckets) {
		this.min = min;
		this.max = max;
		this.buckets = (int) buckets;
	}

	public long count(final String metadata) {
		// if the metadata is null, it counts as a single
		if (metadata == null)
			return 1;
		// otherwise, parse the metadata and count it as that
		else
			return Long.parseLong(metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);
	}

	/** {@inheritDoc} */
	@Override
	public abstract void aggregate(final String data, final String metadata) throws NumberFormatException, IOException, InterruptedException;

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		if (this.isCombining()) {
			// if we're in the combiner, just output the compressed data
			for (final Pair<Number, Long> p : this.getTuples())
				this.collect(p.getFirst().toString(), p.getSecond().toString());
		} else {
			// otherwise, set up the histogram
			int[] buckets = new int[this.buckets];
			// calculate the step or the space between the buckets
			double step = (this.max - this.min) / (double) this.buckets;

			// for each of the compressed data points, increment the bucket it
			// belongs to by its cardinality
			for (final Pair<Number, Long> p : this.getTuples())
				buckets[(int) ((p.getFirst().longValue() - this.min) / step)] += p.getSecond();

			this.collect(Arrays.toString(buckets));
		}
	}

	/**
	 * Return the data points from the dataset in pairs.
	 * 
	 * @return A {@link List} of {@link Pair}&lt{@link Number}, {@link Long}&gt;
	 *         containing the data points from the dataset
	 */
	public abstract List<Pair<Number, Long>> getTuples();
}
