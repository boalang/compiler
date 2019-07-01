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

import org.apache.hadoop.util.bloom.Key;

import boa.io.EmitKey;
import boa.output.Output.Value;

/**
 * A Boa aggregator to estimate the size of the set of unique values in a
 * dataset. Roughly equivalent to a count(distinct(*)).
 *
 * @author anthonyu
 * @author rdyer
 */
@AggregatorSpec(name = "unique", formalParameters = { "int" }, canCombine = true)
public class UniqueAggregator extends DistinctAggregator {
	private long total;

	/**
	 * Construct a UniqueAggregator.
	 *
	 * @param arg
	 *            The size of the internal table used to perform the
	 *            calculation.
	 */
	public UniqueAggregator(final long arg) {
		super(arg);
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		// clear out the internal total
		this.total = 0;
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final Value data, final Value metadata) throws IOException, InterruptedException {
		// instantiate a bloom filter input key initialized by the data
		final Key key = new Key(data.toByteArray());

		// if the key is already in the filter, forget about it
		if (this.filter.membershipTest(key))
			return;

		// add the key to the bloom filter
		this.filter.add(key);

		if (this.isCombining())
			this.collect(data, metadata);
		else
			this.total++;
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		// if we are reducing, collect the total
		if (!this.isCombining())
			this.collect(this.total);
	}
}
