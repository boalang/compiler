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

import java.util.Random;

/**
 * The Boa implementations of the Sawzall math intrinsics that are not in the
 * Java Math class.
 * 
 * The intrinsics that are implemented by the Java Math class are specified in
 * {@link SymbolTable}.
 * 
 * @author anthonyu
 */
public class BoaMathIntrinsics {
	private static Random random = new Random();

	/**
	 * Return a random floating point number x in the range 0.0 < x < 1.0.
	 * 
	 * @return A random floating point number x in the range 0.0 < x < 1.0
	 */
	@FunctionSpec(name = "rand", returnType = "float")
	public static double rand() {
		return BoaMathIntrinsics.random.nextDouble();
	}

	/**
	 * Return a random integer x in the range 0 <= x < n.
	 * 
	 * @param a
	 *            A int representing one greater than the maximum value desired
	 * 
	 * @return A random integer x in the range 0 <= x < n
	 * 
	 * @throws IllegalArgumentException
	 *             When n < 1
	 */
	@FunctionSpec(name = "nrand", returnType = "int", formalParameters = { "int" })
	public static long nRand(final long n) {
		if (n < 1)
			throw new IllegalArgumentException("n must be greater than zero");

		return (long) (BoaMathIntrinsics.random.nextDouble() * n);
	}

	/**
	 * Round to the nearest integer not larger in absolute value.
	 * 
	 * @param a
	 *            A double
	 * 
	 * @return The nearest integer to <em>a</em> not larger in absolute value
	 */
	@FunctionSpec(name = "trunc", returnType = "float", formalParameters = { "float" })
	public static double trunc(final double a) {
		if (a == 0.0 || Double.isNaN(a) || Double.isInfinite(a))
			return a;

		if (a < 0.0)
			return Math.ceil(a);

		if (a > 0.0)
			return Math.floor(a);

		throw new IllegalArgumentException("this should be unreachable");
	}

	/**
	 * The hyperbolic arc sine function.
	 * 
	 * @param d
	 *            A double
	 * 
	 * @return The hyperbolic arc sine of <em>d</em>
	 */
	@FunctionSpec(name = "asinh", returnType = "float", formalParameters = { "float" })
	public static double asinh(final double d) {
		return Math.log(d + Math.sqrt(1.0 + d * d));
	}

	/**
	 * The hyperbolic arc cosine function.
	 * 
	 * @param d
	 *            A double
	 * 
	 * @return The hyperbolic arc cosine of <em>d</em>
	 */
	@FunctionSpec(name = "acosh", returnType = "float", formalParameters = { "float" })
	public static double acosh(final double d) {
		return Math.log(d + (d + 1.0) * Math.sqrt((d - 1.0) / (d + 1.0)));
	}

	/**
	 * The hyperbolic arc tangent function.
	 * 
	 * @param d
	 *            A double
	 * 
	 * @return The hyperbolic arc tangent of <em>d</em>
	 */
	@FunctionSpec(name = "atanh", returnType = "float", formalParameters = { "float" })
	public static double atanh(final double d) {
		return Math.log((1.0 + d) * Math.sqrt(1.0 / (1.0 - d * d)));
	}

	/**
	 * Tests if a double value is NaN.
	 * 
	 * @param v
	 *            A double
	 * 
	 * @return True if <em>v</em> is NaN, false otherwise
	 */
	@FunctionSpec(name = "isnan", returnType = "bool", formalParameters = { "float" })
	public static boolean isNaN(final double v) {
		return Double.isNaN(v);
	}

	/**
	 * Tests if a double value is infinite.
	 * 
	 * @param v
	 *            A double
	 * 
	 * @return True if <em>v</em> is infinite, false otherwise
	 */
	@FunctionSpec(name = "isinf", returnType = "bool", formalParameters = { "float" })
	public static boolean isInfinite(final double v) {
		return Double.isInfinite(v);
	}

	/**
	 * Tests if a double value is not infinite or NaN.
	 * 
	 * @param v
	 *            A double
	 * 
	 * @return True if <em>v</em> is neither infinite nor NaN, false otherwise
	 */
	@FunctionSpec(name = "isfinite", returnType = "bool", formalParameters = { "float" })
	public static boolean isFinite(final double v) {
		return !BoaMathIntrinsics.isNaN(v) && !BoaMathIntrinsics.isInfinite(v);
	}

	/**
	 * Tests if a float value is neither zero, subnormal, infinite, nor NaN.
	 * 
	 * @param v
	 *            A double
	 * 
	 * @return True if <em>v</em> is neither zero, subnormal, infinite, nor NaN;
	 *         false otherwise
	 */
	@FunctionSpec(name = "isnormal", returnType = "bool", formalParameters = { "float" })
	public static boolean isNormal(final double v) {
		return v != 0.0 && v > 0x0.fffffffffffffp-1022 && !BoaMathIntrinsics.isNaN(v) && !BoaMathIntrinsics.isInfinite(v);
	}
}
