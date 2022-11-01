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
import boa.aggregators.SGCounter;

/**
 * A Boa aggregator to count through subgraphs.
 * CSGCounter = "Closed Sub-Graph Counter"
 * @author DavidMOBrien
 */
@AggregatorSpec(name = "csgcounter", formalParameters = { "double" }, canCombine = true)
public class CSGCounter extends SGCounter {
	public CSGCounter(final double n) {
		super(n);
	}

	public HashMap<String, Integer> filter() {
		HashMap<String, Integer> subpatterns = super.filter();

		//TODO: find something not O(n^2)?
		HashMap<String, Integer> filtered = new HashMap<String, Integer>();

		for (String key : subpatterns.keySet()) {
			boolean keep = true;

			for (String compare : subpatterns.keySet()) {
				if (key.equals(compare)) {
					continue;
				}

				//CGspan removes any subpatterns which is a subset of another pattern that has the
				//same frequency. This provides more fruitful results.
				if (compare.startsWith(key) && subpatterns.get(key).equals(subpatterns.get(compare))) {
					keep = false;
				}
			}

			if (keep) {
				filtered.put(key, subpatterns.get(key));
			}
		}

		return filtered;
	}
}
