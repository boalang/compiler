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

/**
 * A Boa aggregator to calculate the bottom <i>n</i> values in a dataset by
 * weight.
 * 
 * @author anthonyu
 */
@AggregatorSpec(name = "minimum", formalParameters = { "int" }, weightType = "float", canOmitWeight = true, canCombine = true)
public class MinimumAggregator extends MinOrMaxAggregator {
	/**
	 * Construct a {@link MinimumAggregator}.
	 * 
	 * @param n A long representing the number of values to return
	 */
	public MinimumAggregator(final long n) {
		super(n);

		DefaultWeight = Double.MAX_VALUE;
	}

	/** {@inheritDoc} */
	@Override
	public int compare(final WeightedString a, final WeightedString b) {
		final double delta = b.getWeight() - a.getWeight();

		// if the weights are different, return the difference
		if (Math.abs(delta) > 0)
			return (int) Math.ceil(delta);

		// otherwise compare the strings
		return b.getString().compareTo(a.getString());
	}
}
