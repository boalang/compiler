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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import boa.io.EmitKey;

/**
 * A Boa aggregator to filter the values in a dataset by maximum size.
 * 
 * @author anthonyu
 */
@AggregatorSpec(name = "set", canCombine = true)
public class SetAggregator extends Aggregator {
	private Set<String> set;
	private final long max;

	/**
	 * Construct a SetAggregator.
	 */
	public SetAggregator() {
		super();

		// allow all values
		this.max = Long.MAX_VALUE;
	}

	/**
	 * Construct a SetAggregator.
	 * 
	 * @param n
	 *            A long representing the number of values to return
	 */
	public SetAggregator(final long n) {
		super(n);

		// the maximum size we will pass through
		this.max = n;
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		// the set of data to be collected
		this.set = new LinkedHashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException, FinishedException {
		if (this.set.size() >= this.max)
			throw new FinishedException();

		this.set.add(data);
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		final List<String> list = new ArrayList<String>(this.set);
		Collections.sort(list);
		for (final String s : list)
			this.collect(s);
	}
}
