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

import java.io.IOException;

import boa.io.EmitKey;
import boa.output.Output.Value;

/**
 * A Boa aggregator to output graph data in GraphViz format.
 *
 * @author rdyer
 */
@AggregatorSpec(name = "graphviz", weightType = "any", canCombine = true)
public class GraphvizAggregator extends GraphAggregator {
	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		for (final Value v : this.neighbors)
			this.collect(v, weights.get(v));
	}

	/** {@inheritDoc} */
	@Override
	protected String format(final String idx, final Value data, final Value metadata) {
		if (metadata == null)
			return "\"" + idx.substring(1, idx.length() - 1)  + "\" -> \"" + EmitKey.valueToString(data) + "\";";
		return "\"" + idx.substring(1, idx.length() - 1)  + "\" -> \"" + EmitKey.valueToString(data) + "\" [label=\"" + EmitKey.valueToString(metadata) + "\"];";
	}
}
