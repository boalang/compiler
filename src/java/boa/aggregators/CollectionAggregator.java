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

import boa.output.Output.Value;

/**
 * A Boa aggregator to output all of the values in a dataset.
 *
 * @author anthonyu
 * @author rdyer
 */
@AggregatorSpec(name = "collection")
public class CollectionAggregator extends Aggregator {
	/** {@inheritDoc} */
	@Override
	public void aggregate(final Value data, final Value metadata) throws IOException, InterruptedException {
		// just pass it through
		this.collect(data, metadata);
	}
}
