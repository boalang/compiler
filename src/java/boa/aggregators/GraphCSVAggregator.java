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


/**
 * A Boa aggregator to output graph data in CSV format as an adjacency list.
 * 
 * @author rdyer
 */
@AggregatorSpec(name = "graph", canCombine = true)
public class GraphCSVAggregator extends GraphAggregator {
	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		this.collect(toString(this.neighbors));
	}

	private String toString(final Set<String> set) {
		String str = "";
		for (final String s : set) {
			if (!str.isEmpty())
				str += ",";
			str += s;
		}
		return str;
	}

	/** {@inheritDoc} */
	@Override
	protected String format(final String idx, final String data, final String metadata) {
		return "\"" + idx.substring(1, idx.length() - 1)  + "\"," + data + "";
	}
}
