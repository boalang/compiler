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

import boa.io.EmitKey;

/**
 * A Boa aggregator to calculate a mean of the values in a dataset.
 * 
 * @author anthonyu
 */
abstract class MeanAggregator extends Aggregator {
	private long count;

	public void count(final String metadata) {
		if (metadata == null)
			this.count++;
		else
			this.count += Long.parseLong(metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.count = 0;
	}

	/**
	 * Return the count of the values in the dataset.
	 * 
	 * @return A long representing the cardinality of the dataset
	 */
	protected long getCount() {
		return this.count;
	}
}
