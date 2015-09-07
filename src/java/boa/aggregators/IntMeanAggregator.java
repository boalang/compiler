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

import boa.functions.BoaCasts;
import boa.io.EmitKey;

/**
 * A Boa aggregator to calculate a mean of the values in a dataset.
 * 
 * @author anthonyu
 */
@AggregatorSpec(name = "mean", type = "int", canCombine = true)
public class IntMeanAggregator extends MeanAggregator {
	private long sum;

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.sum = 0;
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException {
		this.aggregate(Double.valueOf(data).longValue(), metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final long data, final String metadata) {
		this.sum += data;

		super.count(metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final double data, final String metadata) {
		this.aggregate(Double.valueOf(data).longValue(), metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		// if we are in the combiner, output the sum and the count
		if (this.isCombining())
			this.collect(this.sum, BoaCasts.longToString(this.getCount()));
		// otherwise, output the final answer
		else
			this.collect(this.sum / (double) this.getCount());
	}
}
