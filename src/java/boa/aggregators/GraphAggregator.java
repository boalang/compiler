/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
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


/**
 * The base class for all graph output aggregators.
 * 
 * @author rdyer
 */
public abstract class GraphAggregator extends Aggregator {
	protected Set<String> neighbors;
	protected Map<String,String> weights;

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.neighbors = new HashSet<String>();
		this.weights = new HashMap<String,String>();
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException, FinishedException {
		final String neighbor = data.startsWith("\"") ? data : "\"" + data + "\"";
		this.neighbors.add(neighbor);
		final String weight = metadata == null ? null : (metadata.startsWith("\"") ? metadata : "\"" + metadata + "\"");
		this.weights.put(neighbor, weight);
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	protected void collect(final String data, final String metadata) throws IOException, InterruptedException {
		if (this.isCombining()) {
			this.getContext().write(this.getKey(), new EmitValue(data, metadata));
			return;
		}
		this.getContext().write(new Text(format(this.getKey().getIndex(), data, metadata)), NullWritable.get());
	}

	protected abstract String format(final String idx, final String data, final String metadata);
}
