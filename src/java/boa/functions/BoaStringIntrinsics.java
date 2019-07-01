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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String Manipulation
 * 
 * @author anthonyu
 * @author rdyer
 */
public class BoaStringIntrinsics {
	/**
	 * Returns a copy of the given {@link String} with all leading and trailing whitespace removed.
	 * 
	 * @param s
	 *            A {@link String} to remove whitespace
	 * 
	 * @return A copy of <i>s</i> with all leading and trailing whitespace removed.
	 * 
	 */
	@FunctionSpec(name = "trim", returnType = "string", formalParameters = { "string" })
	public static String trim(final String s) {
		return s.trim();
	}

	/**
	 * Returns a copy of the given {@link String} with all characters lowered.
	 * 
	 * @param s
	 *            A {@link String} that wants lowercasing
	 * 
	 * @return A copy of <i>s</i> with all characters converted to lower case,
	 *         as defined by Unicode.
	 * 
	 */
	@FunctionSpec(name = "lowercase", returnType = "string", formalParameters = { "string" })
	public static String lowerCase(final String s) {
		return s.toLowerCase();
	}

	/**
	 * Returns a copy of the given {@link String} with all characters uppered.
	 * 
	 * @param s
	 *            A {@link String} that wants uppercasing
	 * 
	 * @return A copy of <i>s</i> with all characters converted to upper case,
	 *         as defined by Unicode.
	 * 
	 */
	@FunctionSpec(name = "uppercase", returnType = "string", formalParameters = { "string" })
	public static String upperCase(final String s) {
		return s.toUpperCase();
	}

	/**
	 * Search for the first occurrence of the literal string p within s and
	 * return the integer index of its first character, or -1 if it does not
	 * occur.
	 * 
	 * @param p
	 *            A {@link String} containing the needle
	 * 
	 * @param s
	 *            A {@link String} containing the haystack
	 * 
	 * @return A long representing the first occurrence of the literal string
	 *         <em>p</em> within <em>s</em> and return the integer index of its
	 *         first character, or -1 if it does not occur
	 */
	@FunctionSpec(name = "strfind", returnType = "int", formalParameters = { "string", "string" })
	public static long indexOf(final String p, final String s) {
		return s.indexOf(p);
	}

	/**
	 * Search for the last occurrence of the literal string p within s and
	 * return the integer index of its first character, or -1 if it does not
	 * occur.
	 * 
	 * @param p
	 *            A {@link String} containing the needle
	 * 
	 * @param s
	 *            A {@link String} containing the haystack
	 * 
	 * @return A long representing the last occurrence of the literal string
	 *         <em>p</em> within <em>s</em> and return the integer index of its
	 *         first character, or -1 if it does not occur
	 */
	@FunctionSpec(name = "strrfind", returnType = "int", formalParameters = { "string", "string" })
	public static long lastIndexOf(final String p, final String s) {
		return s.lastIndexOf(p);
	}

	/**
	 * Returns the substring of <em>str</em> from <em>start</em> to the end.
	 * 
	 * @param str
	 * @param start
	 * 
	 * @return the substring of <em>str</em>
	 */
	@FunctionSpec(name = "substring", returnType = "string", formalParameters = { "string", "int"})
	public static String substring(final String str, final long start) {
		return str.substring((int)start);
	}

	/**
	 * Returns the substring of <em>str</em> from <em>start</em> inclusive to <em>end</em> exclusive.
	 * 
	 * @param str
	 * @param start
	 * @param end
	 * 
	 * @return the substring of <em>str</em>
	 */
	@FunctionSpec(name = "substring", returnType = "string", formalParameters = { "string", "int", "int"})
	public static String substring(final String str, final long start, final long end) {
		return str.substring((int)start, (int)end);
	}

	/**
	 * Splits a string into an array of strings using the given regex.
	 * 
	 * @param str
	 * @param regex
	 * 
	 * @return the substrings of <em>str</em>, split once by <em>regex</em>
	 */
	@FunctionSpec(name = "split", returnType = "array of string", formalParameters = { "string", "string"})
	public static String[] split(final String str, final String regex) {
		return str.split(regex, 1);
	}

	/**
	 * Splits a string into an array of strings using the given regex.
	 * 
	 * @param str
	 * @param regex
	 * @param n
	 * 
	 * @return the substrings of <em>str</em>, split at most <em>n</em> times by <em>regex</em>
	 */
	@FunctionSpec(name = "splitn", returnType = "array of string", formalParameters = { "string", "string", "int"})
	public static String[] splitn(final String str, final String regex, final long n) {
		return str.split(regex, (int)n);
	}

	/**
	 * Splits a string into an array of strings using the given regex.
	 * 
	 * @param str
	 * @param regex
	 * 
	 * @return the substrings of <em>str</em>, split entirely by <em>regex</em>
	 */
	@FunctionSpec(name = "splitall", returnType = "array of string", formalParameters = { "string", "string"})
	public static String[] splitall(final String str, final String regex) {
		return str.split(regex);
	}

	/**
	 * Return a copy of string <em>str</em>, with non-overlapping instances of
	 * <em>lit</em> replaced by <em>rep</em>. If <em>replace_all</em> is false,
	 * only the first found instance is replaced.
	 * 
	 * @param str
	 *            A {@link String} containing the source string
	 * 
	 * @param lit
	 *            A {@link String} containing the substring to be replaced
	 * 
	 * @param rep
	 *            A {@link String} containing the replacement string
	 * 
	 * @param replaceAll
	 *            A boolean representing whether to replace every instance of
	 *            <em>lit</em> with <em>rep</em>
	 * 
	 * @return A copy of {@link String} <em>str</em>, with non-overlapping
	 *         instances of <em>lit</em> replaced by <em>rep</em>
	 */
	@FunctionSpec(name = "strreplace", returnType = "string", formalParameters = { "string", "string", "string", "bool" })
	public static String stringReplace(final String str, final String lit, final String rep, final boolean replaceAll) {
		if (replaceAll)
			return str.replace(lit, rep);
		else
			return str.replaceFirst(Pattern.quote(lit), rep);
	}

	// cache the regular expression patterns for performance
	private static Map<String, Matcher> matchers = new HashMap<String, Matcher>();

	private static Matcher getMatcher(final String r) {
		if (!matchers.containsKey(r))
			matchers.put(r, Pattern.compile(r).matcher(""));
		return matchers.get(r);
	}

	/**
	 * Search for a match of the regular expression <em>r</em> within <em>s</em>
	 * , and return a boolean value indicating whether a match was found. (The
	 * regular expression syntax is that of PCRE. <http://www.pcre.org/>)
	 * 
	 * @param r
	 *            A {@link String} containing a regular expression
	 * 
	 * @param s
	 *            A {@link String} containing the text to be searched
	 * 
	 * @return A boolean representing whether the regular expression <em>r</em>
	 *         was found within <em>s</em>
	 */
	@FunctionSpec(name = "match", returnType = "bool", formalParameters = { "string", "string" })
	public static boolean match(final String r, final String s) {
		final Matcher m = getMatcher(r).reset(s);
		return m.find();
	}

	/**
	 * Search for a match of the regular expression <em>r</em> within <em>s</em>
	 * , and return an array consisting of character positions within <em>s</em>
	 * defined by the match. Positions 0 and 1 of the array report the location
	 * of the match of the entire expression, subsequent pairs report the
	 * location of matches of successive parenthesized subexpressions.
	 * 
	 * @param r
	 *            A {@link String} containing a regular expression
	 * 
	 * @param s
	 *            A {@link String} containing the text to be searched
	 * 
	 * @return An array of long consisting of character positions within
	 *         <em>s</em> defined by the match
	 */
	@FunctionSpec(name = "matchposns", returnType = "array of int", formalParameters = { "string", "string" })
	public static long[] matchPositions(final String r, final String s) {
		final Matcher m = getMatcher(r).reset(s);

		if (!m.find())
			return new long[0];

		final int n = m.groupCount();

		final long[] matches = new long[(n + 1) * 2];

		for (int i = 0; i <= n; i++) {
			matches[i * 2] = m.start(i);
			matches[i * 2 + 1] = m.end(i);
		}

		return matches;
	}

	/**
	 * Search for a match of the regular expression <em>r</em> within <em>s</em>
	 * , and return . The 0th string is the entire match; following elements of
	 * the array hold matches of successive parenthesized subexpressions. This
	 * function is equivalent to using matchposns to find successive locations
	 * of matches and created array slices of <em>s</em> with the indices
	 * returned.
	 * 
	 * 
	 * @param r
	 *            A {@link String} containing a regular expression
	 * 
	 * @param s
	 *            A {@link String} containing the text to be searched
	 * 
	 * @return an array of {@link String} consisting of matched substrings of
	 *         <em>s</em>
	 */
	@FunctionSpec(name = "matchstrs", returnType = "array of string", formalParameters = { "string", "string" })
	public static String[] matchStrings(final String r, final String s) {
		final Matcher m = getMatcher(r).reset(s);

		if (!m.find())
			return new String[0];

		final int n = m.groupCount();

		final String[] matches = new String[(n + 1)];

		for (int i = 0; i <= n; i++)
			matches[i] = m.group(i);

		return matches;
	}

	/**
	 * Return a string containing the arguments formatted according to the
	 * format string fmt. The syntax of the format string is essentially that of
	 * ANSI C with the following differences:
	 * 
	 * <ul>
	 * <li>%b prints a boolean, "true" or "false".
	 * <li>%c prints a (u)int as a Unicode character in UTF-8.
	 * <li>%k like %c with single quotes and backslash escapes for special
	 * characters.
	 * <li>%s prints a Sawzall string as UTF-8.
	 * <li>%q like %s with double quotes and backslash escapes for special
	 * characters.
	 * <li>%t prints a time, in the format of the Unix function ctime without a
	 * newline.
	 * <li>%T prints a Sawzall type of the argument; %#T expands user-defined
	 * types.
	 * <li>%d / %i / %o / %u / %x / %X apply to a Sawzall (u)int and have no 'l'
	 * or 'h' modifiers.
	 * <li>%e / %f / %g / %E / %G apply to a Sawzall float and have no 'l' or
	 * 'h' modifiers.
	 * </ul>
	 * format verbs 'n' and '*' are not supported.
	 * 
	 * @param format
	 *            A
	 * @param args
	 * 
	 * @return A string containing the arguments formatted according to the
	 *         format string <em>fmt</em>
	 */
	@FunctionSpec(name = "format", returnType = "string", formalParameters = { "string", "any..." })
	public static String format(final String format, final Object... args) {
		// TODO: support the Sawzall differences listed in the javadoc above
		return String.format(format, args);
	}
}
