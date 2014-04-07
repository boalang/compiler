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
 * A pair of values.
 * 
 * @author anthonyu
 * 
 * @param <F> The type of the first value
 * @param <S> The type of the second value
 */
class Pair<F, S> {
	private final F first;
	private final S second;

	/**
	 * Construct a {@link Pair}.
	 * 
	 * @param first The first value
	 * @param second The second value
	 */
	public Pair(final F first, final S second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Get the first value.
	 * 
	 * @return The first value
	 */
	public F getFirst() {
		return this.first;
	}

	/**
	 * Get the second value.
	 * 
	 * @return The second value
	 */
	public S getSecond() {
		return this.second;
	}
}
