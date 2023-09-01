/*
 * Copyright 2014, Anthony Urso, Hridesh Rajan, Robert Dyer,
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
package boa.functions;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Some less trivial casts provided by Boa.
 * 
 * @author anthonyu
 * @author rdyer
 */
public class BoaCasts {
	/**
	 * Convert a {@link String} into a boolean.
	 * 
	 * @param s
	 *            The {@link String} to be converted
	 * 
	 * @return True iff <em>s</em> begins with 'T' or 't'
	 * 
	 */
	public static boolean stringToBoolean(final String s) {
		final char c = s.charAt(0);

		if (c == 'T' || c == 't')
			return true;

		return false;
	}

	/**
	 * Convert a boolean into a String.
	 * 
	 * @param b
	 *            The boolean to be converted
	 * 
	 * @return A String representing the boolean value <em>b</em>
	 */
	public static String booleanToString(final boolean b) {
		if (b)
			return "true";
		return "false";
	}

	/**
	 * Convert a boolean into a long.
	 * 
	 * @param b
	 *            The boolean to be converted
	 * 
	 * @return A long representing the boolean value <em>b</em>
	 */
	public static long booleanToLong(final boolean b) {
		if (b)
			return 1;
		return 0;
	}

	private final static SimpleDateFormat boaDateFormat = new SimpleDateFormat("EEE MMM d yyyy HH:mm:ss z");

	/**
	 * Parse a time string.
	 * 
	 * @param s
	 *            A {@link String} containing a time
	 * 
	 * @return A long containing the time represented by <em>s</em>.
	 * 
	 * @throws RuntimeException if the time string is invalid
	 */
	public static long stringToTime(final String s, final String tz) {
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(tz));

		// first try a standard format
		try {
			boaDateFormat.setCalendar(calendar);
			return boaDateFormat.parse(s).getTime() * 1000;
		} catch (final Exception e) { }

		// then try every possible combination of built in formats
		final int [] formats = new int[] {DateFormat.DEFAULT, DateFormat.FULL, DateFormat.SHORT, DateFormat.LONG, DateFormat.MEDIUM};
		for (final int f : formats)
			for (final int f2 : formats)
				try {
					final DateFormat df = DateFormat.getDateTimeInstance(f, f2);
					df.setCalendar(calendar);
					return df.parse(s).getTime() * 1000;
				} catch (final Exception e) { }

		throw new RuntimeException("unable to convert string to time - bad format for string: " + s);
	}

	/**
	 * Parse a time string.
	 * 
	 * @param s
	 *            A {@link String} containing a time
	 * 
	 * @return A long containing the time represented by <em>s</em>.
	 * 
	 * @throws RuntimeException if the time string is invalid
	 */
	public static long stringToTime(final String s) {
		return BoaCasts.stringToTime(s, "UTC");
	}

	private static final DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

	/**
	 * Format a double into a {@link String}.
	 * 
	 * @param d
	 *            A double
	 * 
	 * @return A {@link String} containing the number <em>d</em>
	 */
	public static String doubleToString(final double d) {
		df.setMaximumFractionDigits(340); // value in private field: DecimalFormat.DOUBLE_FRACTION_DIGITS
		df.setMinimumFractionDigits(1);
		return df.format(d);
	}

	/**
	 * Format a long into a {@link String}.
	 * 
	 * @param l
	 *            A long
	 * 
	 * @return A {@link String} containing the number <em>l</em>
	 */
	public static String longToString(final long l) {
		df.setMaximumFractionDigits(0);
		df.setMinimumFractionDigits(0);
		return df.format(l);
	}

	/**
	 * Format a long into a {@link String} in the given radix.
	 * 
	 * @param l
	 *            A long
	 * 
	 * @param radix
	 *            The desired radix
	 * 
	 * @return A {@link String} containing the number <em>l</em> in base
	 *         <em>radix</em>
	 */
	public static String longToString(final long l, final long radix) {
		return Long.toString(l, (int) radix);
	}

	/**
	 * Format a time string.
	 * 
	 * @param t
	 *            A long containing a time
	 * 
	 * @param tz
	 *            A String containing the time zone to be used for formatting
	 * 
	 * @return A {@link String} containing the time represented by <em>t</em>.
	 */
	public static String timeToString(final long t, final String tz) {
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(tz));

		calendar.setTimeInMillis(t / 1000);

		boaDateFormat.setCalendar(calendar);

		return boaDateFormat.format(calendar.getTime());
	}

	/**
	 * Format a time string.
	 * 
	 * @param t
	 *            A long containing a time
	 * 
	 * @return A {@link String} containing the time represented by <em>t</em>.
	 */
	public static String timeToString(final long t) {
		return BoaCasts.timeToString(t, "UTC");
	}
}
