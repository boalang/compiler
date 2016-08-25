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
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import boa.io.EmitKey;

/**
 * A Boa aggregator to calculate the quantiles for the values in a dataset.
 * 
 * @author anthonyu
 */
@AggregatorSpec(name = "quantile", formalParameters = { "int" }, type = "int", canCombine = true)
public class IntQuantileAggregator extends QuantileAggregator {
	private SortedCountingSet<Long> list;

	/**
	 * Construct a IntQuantileAggregator.
	 * 
	 * @param n
	 *            A long representing the number of quantiles to calculate
	 */
	public IntQuantileAggregator(final long n) {
		super(n);
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.list = new SortedCountingSet<Long>();
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException {
		this.aggregate(Double.valueOf(data).longValue(), metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final long data, final String metadata) throws IOException {
		this.list.add(Long.valueOf(data), super.count(metadata));
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final double data, final String metadata) throws IOException {
		this.aggregate(Double.valueOf(data).longValue(), metadata);
	}

	/** {@inheritDoc} */
	@Override
	public List<Pair<String, Long>> getTuples() {
		final List<Pair<String, Long>> list = new ArrayList<Pair<String, Long>>();

		// convert the map entries into a list of Pair
		for (final Entry<Long, Long> e : this.list.getEntries())
			list.add(new Pair<String, Long>(e.getKey().toString(), e.getValue()));

		return list;
	}
}
