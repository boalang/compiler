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
 * A Boa aggregator to calculate the quantiles for the values in a dataset.
 *
 * @author anthonyu
 * @author rdyer
 */
@AggregatorSpec(name = "quantile", formalParameters = { "int" }, type = "float", canCombine = true)
public class FloatQuantileAggregator extends QuantileAggregator {
	private SortedCountingSet<Double> list;

	/**
	 * Construct a FloatQuantileAggregator.
	 *
	 * @param n
	 *            A long representing the number of quantiles to calculate
	 */
	public FloatQuantileAggregator(final long n) {
		super(n);
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
	public List<Pair<Value, Long>> getTuples() {
		final List<Pair<Value, Long>> list = new ArrayList<Pair<Value, Long>>();

		// convert the map entries into a list of Pair
		for (final Entry<Double, Long> e : this.list.getEntries())
			list.add(new Pair<Value, Long>(EmitKey.toValue(e.getKey()), e.getValue()));

		return list;
	}
}
