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

import boa.functions.BoaCasts;

/**
 * A tuple containing a {@link String} and its weight.
 * 
 * @author anthonyu
 */
class WeightedString {
	private final String string;
	private final double weight;

	/**
	 * Construct a WeightedString.
	 * 
	 * @param string
	 *            A {@link String} containing the string part of the tuple
	 * 
	 * @param weight
	 *            A double representing the weight part of the tuple
	 */
	public WeightedString(final String string, final double weight) {
		this.string = string;
		this.weight = weight;
	}

	/**
	 * Get the string part of the tuple.
	 * 
	 * @return A {@link String} containing the string part of the tuple
	 */
	public String getString() {
		return this.string;
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
		result = prime * result + (this.string == null ? 0 : this.string.hashCode());
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

		final WeightedString other = (WeightedString) obj;

		if (this.string == null && other.string != null)
				return false;

		if (this.string != null && !this.string.equals(other.string))
			return false;

		if (Double.doubleToLongBits(this.weight) != Double.doubleToLongBits(other.weight))
			return false;

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.string + ", " + BoaCasts.doubleToString(this.weight);
	}
}
