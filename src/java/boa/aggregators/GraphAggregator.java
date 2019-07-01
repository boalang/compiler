/*
 * Copyright 2019, Hridesh Rajan, Robert Dyer,
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

import java.util.*;
import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import boa.io.EmitKey;
import boa.io.EmitValue;
import boa.output.Output.Value;

/**
 * The base class for all graph output aggregators.
 *
 * @author rdyer
 */
public abstract class GraphAggregator extends Aggregator {
	protected Set<Value> neighbors;
	protected Map<Value, Value> weights;

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.neighbors = new HashSet<Value>();
		this.weights = new HashMap<Value, Value>();
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final Value data, final Value metadata) throws IOException, InterruptedException, FinishedException {
		this.neighbors.add(data);
		this.weights.put(data, metadata);
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	protected void collect(final Value data, final Value metadata) throws IOException, InterruptedException {
		if (this.isCombining()) {
			this.getContext().write(this.getKey(), new EmitValue(data, metadata));
			return;
		}
		this.getContext().write(NullWritable.get(), this.toRow(EmitKey.toValue(format(this.getKey().getIndex(), data, metadata)), null));
	}

	protected abstract String format(final String idx, final Value data, final Value metadata);
}
