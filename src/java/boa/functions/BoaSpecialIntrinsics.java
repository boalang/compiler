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

import java.util.HashMap;
import java.util.Map;

/**
 * Special Functions
 * 
 * <p>These functions have special properties, such as variable types, variable
 * numbers of parameters, or parameters that are types rather than values. Some
 * of the syntax used to describe them, e.g. "...", default arguments and
 * overloading, is not part of the Sawzall language.
 * 
 * @author anthonyu
 */
public class BoaSpecialIntrinsics {
	private static Map<String, String> regexMap;

	static {
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

	/**
	 * Initializes an array of the given type and size.
	 * 
	 * @param size the size of array
	 * @param val the initial value of each array entry
	 * 
	 * @return The array
	 */
	@FunctionSpec(name = "new", returnType = "array of int", formalParameters = { "array of int", "int", "int" })
	public static Long[] newInt(Long[] a, long size, long val) {
		Long[] arr = new Long[(int)size];
		for (int i = 0; i < size; i++)
			arr[i] = val;
		return arr;
	}
	@FunctionSpec(name = "new", returnType = "array of float", formalParameters = { "array of float", "int", "float" })
	public static Double[] newDouble(Double[] a, long size, double val) {
		Double[] arr = new Double[(int)size];
		for (int i = 0; i < size; i++)
			arr[i] = val;
		return arr;
	}
	@FunctionSpec(name = "new", returnType = "array of bool", formalParameters = { "array of bool", "int", "bool" })
	public static Boolean[] newBoolean(Boolean[] a, long size, boolean val) {
		Boolean[] arr = new Boolean[(int)size];
		for (int i = 0; i < size; i++)
			arr[i] = val;
		return arr;
	}
	@FunctionSpec(name = "new", returnType = "array of time", formalParameters = { "array of time", "int", "time" })
	public static Long[] newTime(Long[] a, long size, long val) {
		Long[] arr = new Long[(int)size];
		for (int i = 0; i < size; i++)
			arr[i] = val;
		return arr;
	}
	@FunctionSpec(name = "new", returnType = "array of string", formalParameters = { "array of string", "int", "string" })
	public static String[] newString(String[] a, long size, String val) {
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
