/*
 * Copyright 2022, Hridesh Rajan, David M. OBrien,
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
import java.util.HashMap;

import boa.functions.BoaCasts;
import boa.io.EmitKey;

/**
 * A Boa aggregator to count through subpatterns.
 * SGCounter = "Sub-Graph Counter"
 * @author DavidMOBrien
 */
@AggregatorSpec(name = "sgcounter", formalParameters = { "double" }, canCombine = true)
public class SGCounter extends MeanAggregator {
	private HashMap<String, Integer> results;
	private double freq;

	public SGCounter(final double n) {
		this.freq = n;
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		results = new HashMap<String, Integer>();
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException {
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final HashMap<String, Integer> data, final String metadata) throws IOException, InterruptedException, FinishedException {
		this.count(metadata);

		for (final String key: data.keySet()) {
			if (results.containsKey(key)) {
				results.put(key, results.get(key) + data.get(key));
			} else {
				results.put(key, data.get(key));
			}
		}
	}

	public HashMap<String, Integer> filter() {
		final HashMap<String, Integer> temp = new HashMap<String, Integer>();

		final double minimum = this.getCount() * this.freq;

		//FIXME: find a better way than creating an entirely new HashMap
		//     however, we get a concurrency error if we try using .remove
		for (final String key: this.results.keySet()) {
			if (this.results.get(key) > minimum) {
				temp.put(key, this.results.get(key));
			}
		}

		return temp;
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		// if we are in the combiner, output the sum and the count
		if (this.isCombining()) {
			this.collect(this.results, BoaCasts.longToString(this.getCount()));
		} else {
			// otherwise, output the final answer
			this.collect(filter().toString());
		}
	}
}
