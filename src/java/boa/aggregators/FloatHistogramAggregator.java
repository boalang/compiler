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
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import boa.io.EmitKey;
import boa.output.Output.Value;

/**
 * A Boa aggregator to calculate a histogram for the values in a dataset.
 *
 * @author anthonyu
 * @author rdyer
 */
@AggregatorSpec(name = "histogram", formalParameters = { "int", "int", "int" }, type = "float", canCombine = true)
public class FloatHistogramAggregator extends HistogramAggregator {
	private SortedCountingSet<Double> list;

	/**
	 * Construct a FloatHistogramAggregator.
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
	public FloatHistogramAggregator(final long min, final long max, final long buckets) {
		super(min, max, buckets);
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.list = new SortedCountingSet<Double>();
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final double data, final Value metadata) throws IOException {
		this.list.add(data, super.count(metadata));
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		if (this.isCombining()) {
			// if we're in the combiner, just output the compressed data
			for (final Pair<Number, Long> p : this.getTuples())
				this.collect(EmitKey.toValue((Double)p.getFirst()), EmitKey.toValue(p.getSecond()));
		} else {
			super.finish();
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<Pair<Number, Long>> getTuples() {
		final List<Pair<Number, Long>> list = new ArrayList<Pair<Number, Long>>();

		// convert the map entries into a list of Pair
		for (final Entry<Double, Long> e : this.list.getEntries())
			list.add(new Pair<Number, Long>(e.getKey(), e.getValue()));

		return list;
	}
}
