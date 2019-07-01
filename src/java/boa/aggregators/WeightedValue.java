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

import boa.functions.BoaCasts;
import boa.io.EmitKey;
import boa.output.Output.Value;

/**
 * A tuple containing a {@link Value} and its weight.
 *
 * @author anthonyu
 * @author rdyer
 */
class WeightedValue {
	private final Value v;
	private final double weight;

	/**
	 * Construct a WeightedValue.
	 *
	 * @param v
	 *            A {@link Value} containing the value part of the tuple
	 *
	 * @param weight
	 *            A double representing the weight part of the tuple
	 */
	public WeightedValue(final Value v, final double weight) {
		this.v = v;
		this.weight = weight;
	}

	/**
	 * Get the value part of the tuple.
	 *
	 * @return A {@link Value} containing the value part of the tuple
	 */
	public Value getValue() {
		return this.v;
	}

	/**
	 * Get the weight part of the tuple.
	 *
	 * @return A double containing the weight part of the tuple
	 */
	public double getWeight() {
		return this.weight;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.v == null ? 0 : this.v.hashCode());
		final long temp = Double.doubleToLongBits(this.weight);
		result = prime * result + (int) (temp ^ temp >>> 32);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;

		if (obj == null || this.getClass() != obj.getClass())
			return false;

		final WeightedValue other = (WeightedValue) obj;

		if (this.v == null && other.v != null)
				return false;

		if (this.v != null && !this.v.equals(other.v))
			return false;

		if (Double.doubleToLongBits(this.weight) != Double.doubleToLongBits(other.weight))
			return false;

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return EmitKey.valueToString(this.v) + ", " + BoaCasts.doubleToString(this.weight);
	}
}
