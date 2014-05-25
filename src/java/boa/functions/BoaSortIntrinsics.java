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
package boa.functions;

import java.util.Arrays;

/**
 * Built-in functions for sorting data.
 * 
 * @author anthonyu
 */
public class BoaSortIntrinsics {
	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of long
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of int", formalParameters = { "array of int" })
	public static long[] sortLongArray(final long[] a) {
		final long[] b = Arrays.copyOf(a, a.length);

		Arrays.sort(b);

		return b;
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of long
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of int", formalParameters = { "array of int", "string" })
	public static long[] sortLongArray(final long[] a, final String ignored) {
		return BoaSortIntrinsics.sortLongArray(a);
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of double
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of float", formalParameters = { "array of float" })
	public static double[] sortDoubleArray(final double[] a) {
		final double[] b = Arrays.copyOf(a, a.length);

		Arrays.sort(b);

		return b;
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of double
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of float", formalParameters = { "array of float", "string" })
	public static double[] sortDoubleArray(final double[] a, final String ignored) {
		return BoaSortIntrinsics.sortDoubleArray(a);
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of {@link String}
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of string", formalParameters = { "array of string" })
	public static String[] sortStringArray(final String[] a) {
		final String[] b = Arrays.copyOf(a, a.length);

		Arrays.sort(b);

		return b;
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of {@link String}
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of string", formalParameters = { "array of string", "string" })
	public static String[] sortStringArray(final String[] a, final String ignored) {
		return BoaSortIntrinsics.sortStringArray(a);
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of long
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of time", formalParameters = { "array of time" })
	public static long[] sortTimeArray(final long[] a) {
		return BoaSortIntrinsics.sortLongArray(a);
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of long
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of time", formalParameters = { "array of time", "string" })
	public static long[] sortTimeArray(final long[] a, final String ignored) {
		return BoaSortIntrinsics.sortTimeArray(a);
	}

	// TODO: implement sortx
}
