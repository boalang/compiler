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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Special Functions
 * 
 * These functions have special properties, such as variable types, variable
 * numbers of parameters, or parameters that are types rather than values. Some
 * of the syntax used to describe them, e.g. "...", default arguments and
 * overloading, is not part of the Sawzall language.
 * 
 * @author anthonyu
 */
public class BoaSpecialIntrinsics {
	private static MessageDigest md;
	private static Map<String, String> regexMap;

	static {
		try {
			BoaSpecialIntrinsics.md = MessageDigest.getInstance("SHA");
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		BoaSpecialIntrinsics.regexMap = new HashMap<String, String>();
		BoaSpecialIntrinsics.regexMap.put("int,16", "(0x)?[A-Fa-f0-9]+h?");
		BoaSpecialIntrinsics.regexMap.put("int,10", "[+-]?[0-9]+");
		BoaSpecialIntrinsics.regexMap.put("int,8", "0[0-7]+");
		BoaSpecialIntrinsics.regexMap.put("string", "\\S+");
		BoaSpecialIntrinsics.regexMap.put("time", "[0-9]+");
		BoaSpecialIntrinsics.regexMap.put("float", "[-+]?[0-9]*\\.?[0-9]+(e[-+]?[0-9]+)?");
	}

	/**
	 * If <em>condition</em> is false, print the <em>message</em> to standard
	 * error, with the prefix assertion failed:, and exit. The message may be
	 * empty or absent altogether.
	 * 
	 * @param condition
	 *            The condition to be checked
	 * 
	 * @param message
	 *            A {@link String} containing the message to be printed upon
	 *            failure
	 * 
	 * @return True iff <em>condition</em> is true
	 */
	@FunctionSpec(name = "assert", formalParameters = { "bool", "string" })
	public static void azzert(final boolean condition, final String message) {
		if (!condition)
			throw new RuntimeException("assertion failed: " + message);
	}

	/**
	 * If <em>condition</em> is false, print the <em>message</em> to standard
	 * error, with the prefix assertion failed:, and exit. The message may be
	 * empty or absent altogether.
	 * 
	 * @param condition
	 *            The condition to be checked
	 * 
	 * @return True iff <em>condition</em> is true
	 */
	@FunctionSpec(name = "assert", formalParameters = { "bool" })
	public static void azzert(final boolean condition) {
		if (!condition)
			throw new RuntimeException("assertion failed");
	}

	private static byte[] longToByteArray(final long l) {
		return new byte[] { (byte) (l >> 56 & 0xff), (byte) (l >> 48 & 0xff), (byte) (l >> 40 & 0xff), (byte) (l >> 32 & 0xff), (byte) (l >> 24 & 0xff),
				(byte) (l >> 16 & 0xff), (byte) (l >> 8 & 0xff), (byte) (l >> 0 & 0xff), };
	}

	private static long byteArrayToLong(final byte[] bs) {
		return (long) (0xff & bs[0]) << 56 | (long) (0xff & bs[1]) << 48 | (long) (0xff & bs[2]) << 40 | (long) (0xff & bs[3]) << 32
				| (long) (0xff & bs[4]) << 24 | (long) (0xff & bs[5]) << 16 | (long) (0xff & bs[6]) << 8 | (long) (0xff & bs[7]) << 0;
	}

	/**
	 * Initializes an array of the given type and size.
	 * 
	 * @param size the size of array
	 * @param val the initial value of each array entry
	 * 
	 * @return The array
	 */
	@FunctionSpec(name = "new", returnType = "array of int", formalParameters = { "array of int", "int", "int" })
	public static long[] newInt(long[] a, long size, long val) {
		long[] arr = new long[(int)size];
		for (int i = 0; i < size; i++)
			arr[i] = val;
		return arr;
	}
	@FunctionSpec(name = "new", returnType = "array of float", formalParameters = { "array of float", "int", "float" })
	public static double[] newDouble(double[] a, long size, double val) {
		double[] arr = new double[(int)size];
		for (int i = 0; i < size; i++)
			arr[i] = val;
		return arr;
	}
	@FunctionSpec(name = "new", returnType = "array of bool", formalParameters = { "array of bool", "int", "bool" })
	public static boolean[] newBoolean(boolean[] a, long size, boolean val) {
		boolean[] arr = new boolean[(int)size];
		for (int i = 0; i < size; i++)
			arr[i] = val;
		return arr;
	}
	@FunctionSpec(name = "new", returnType = "array of time", formalParameters = { "array of time", "int", "time" })
	public static long[] newTime(long[] a, long size, long val) {
		long[] arr = new long[(int)size];
		for (int i = 0; i < size; i++)
			arr[i] = val;
		return arr;
	}
	@FunctionSpec(name = "new", returnType = "array of string", formalParameters = { "array of string", "int", "string" })
	public static String[] newBoolean(String[] a, long size, String val) {
		String[] arr = new String[(int)size];
		for (int i = 0; i < size; i++)
			arr[i] = val;
		return arr;
	}

	public static String regex(final String type, final long base) {
		if (BoaSpecialIntrinsics.regexMap.containsKey(type + "," + base))
			return BoaSpecialIntrinsics.regexMap.get(type + "," + base);
		else
			throw new RuntimeException("unimplemented");
	}

	public static String regex(final String type) {
		if (BoaSpecialIntrinsics.regexMap.containsKey(type))
			return BoaSpecialIntrinsics.regexMap.get(type);
		else
			throw new RuntimeException("unimplemented");
	}
}
