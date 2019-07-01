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

import boa.io.EmitKey;
import boa.output.Output.Value;

/**
 * A Boa aggregator to calculate a mean of the values in a dataset.
 *
 * @author anthonyu
 * @author rdyer
 */
abstract class MeanAggregator extends Aggregator {
	private long count;

	public void count(final Value metadata) {
		if (metadata == null) {
			this.count++;
		} else {
			if (metadata.getType() == Value.Type.INT)
				this.count += metadata.getI();
			else if (metadata.getType() == Value.Type.FLOAT)
				this.count += metadata.getF();
		}
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
