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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * These functions manipulate time values. Although one may do simple arithmetic
 * to add a minute, say, because of daylight saving time, leap years, and other
 * inconveniences many such operations require more sophistication, which these
 * functions provide.
 * 
 * Most time functions accept an optional argument indicating the time zone; the
 * default time zone is PST8PDT.
 * 
 * @author anthonyu
 */
public class BoaTimeIntrinsics {
	private static Map<Character, SimpleDateFormat> strftimeMap = new HashMap<Character, SimpleDateFormat>();

	static {
		BoaTimeIntrinsics.strftimeMap.put('a', new SimpleDateFormat("E"));
		BoaTimeIntrinsics.strftimeMap.put('A', new SimpleDateFormat("EEEE"));
		BoaTimeIntrinsics.strftimeMap.put('b', new SimpleDateFormat("MMM"));
		BoaTimeIntrinsics.strftimeMap.put('B', new SimpleDateFormat("MMMM"));
		BoaTimeIntrinsics.strftimeMap.put('c', new SimpleDateFormat("E MMM d HH:mm:ss yyyy"));
		BoaTimeIntrinsics.strftimeMap.put('d', new SimpleDateFormat("dd"));
		BoaTimeIntrinsics.strftimeMap.put('H', new SimpleDateFormat("HH"));
		BoaTimeIntrinsics.strftimeMap.put('I', new SimpleDateFormat("hh"));
		BoaTimeIntrinsics.strftimeMap.put('j', new SimpleDateFormat("DDD"));
		BoaTimeIntrinsics.strftimeMap.put('m', new SimpleDateFormat("MM"));
		BoaTimeIntrinsics.strftimeMap.put('M', new SimpleDateFormat("mm"));
		BoaTimeIntrinsics.strftimeMap.put('p', new SimpleDateFormat("aa"));
		BoaTimeIntrinsics.strftimeMap.put('S', new SimpleDateFormat("ss"));
		BoaTimeIntrinsics.strftimeMap.put('U', new SimpleDateFormat("ww"));
		BoaTimeIntrinsics.strftimeMap.put('w', new SimpleDateFormat("F"));
		BoaTimeIntrinsics.strftimeMap.put('W', new SimpleDateFormat("ww"));
		BoaTimeIntrinsics.strftimeMap.put('x', new SimpleDateFormat("MM/dd/yy"));
		BoaTimeIntrinsics.strftimeMap.put('X', new SimpleDateFormat("HH:mm:ss"));
		BoaTimeIntrinsics.strftimeMap.put('y', new SimpleDateFormat("yy"));
		BoaTimeIntrinsics.strftimeMap.put('Y', new SimpleDateFormat("yyyy"));
		BoaTimeIntrinsics.strftimeMap.put('Z', new SimpleDateFormat("zzz"));
	}

	private static long addPart(final int part, final long t, final long n, final TimeZone tz) {
		final Calendar calendar = Calendar.getInstance(tz);

		calendar.setTimeInMillis(t / 1000);

		calendar.add(part, (int) n);

		return calendar.getTimeInMillis() * 1000;
	}

	/**
	 * Return the time n days after t. The value of n may be negative, or n may
	 * be absent altogether (addday(t)), in which case n defaults to 1. An
	 * optional third argument, a string, names a timezone.
	 * 
	 * @param t
	 *            A long representing the initial time
	 * 
	 * @param n
	 *            A long representing the number of days to add
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the time with the specified number of days
	 *         added to it
	 */
	@FunctionSpec(name = "addday", returnType = "time", formalParameters = { "time", "int", "string" })
	public static long addDay(final long t, final long n, final String tz) {
		return BoaTimeIntrinsics.addPart(Calendar.DAY_OF_MONTH, t, n, TimeZone.getTimeZone(tz));
	}

	/**
	 * Return the time n days after t. The value of n may be negative, or n may
	 * be absent altogether (addday(t)), in which case n defaults to 1. An
	 * optional third argument, a string, names a timezone.
	 * 
	 * @param t
	 *            A long representing the initial time
	 * 
	 * @param n
	 *            A long representing the number of days to add
	 * 
	 * @return A long representing the time with the specified number of days
	 *         added to it
	 */
	@FunctionSpec(name = "addday", returnType = "time", formalParameters = { "time", "int" })
	public static long addDay(final long t, final long n) {
		return BoaTimeIntrinsics.addPart(Calendar.DAY_OF_MONTH, t, n, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * Return the time n days after t. The value of n may be negative, or n may
	 * be absent altogether (addday(t)), in which case n defaults to 1. An
	 * optional third argument, a string, names a timezone.
	 * 
	 * @param t
	 *            A long representing the initial time
	 * 
	 * @return A long representing the time with the specified number of days
	 *         added to it
	 */
	@FunctionSpec(name = "addday", returnType = "time", formalParameters = { "time" })
	public static long addDay(final long t) {
		return BoaTimeIntrinsics.addPart(Calendar.DAY_OF_MONTH, t, 1, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * Like addday, but for months.
	 * 
	 * @param t
	 *            A long representing the initial time
	 * 
	 * @param n
	 *            A long representing the number of months to add
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the time with the specified number of months
	 *         added to it
	 */
	@FunctionSpec(name = "addmonth", returnType = "time", formalParameters = { "time", "int", "string" })
	public static long addMonth(final long t, final long n, final String tz) {
		return BoaTimeIntrinsics.addPart(Calendar.MONTH, t, n, TimeZone.getTimeZone(tz));
	}

	/**
	 * Like addday, but for months.
	 * 
	 * @param t
	 *            A long representing the initial time
	 * 
	 * @param n
	 *            A long representing the number of months to add
	 * 
	 * @return A long representing the time with the specified number of months
	 *         added to it
	 */
	@FunctionSpec(name = "addmonth", returnType = "time", formalParameters = { "time", "int" })
	public static long addMonth(final long t, final long n) {
		return BoaTimeIntrinsics.addPart(Calendar.MONTH, t, n, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * Like addday, but for months.
	 * 
	 * @param t
	 *            A long representing the initial time
	 * 
	 * @return A long representing the time with the specified number of months
	 *         added to it
	 */
	@FunctionSpec(name = "addmonth", returnType = "time", formalParameters = { "time" })
	public static long addMonth(final long t) {
		return BoaTimeIntrinsics.addPart(Calendar.MONTH, t, 1, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * Like addday, but for weeks.
	 * 
	 * @param t
	 *            A long representing the initial time
	 * 
	 * @param n
	 *            A long representing the number of weeks to add
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the time with the specified number of weeks
	 *         added to it
	 */
	@FunctionSpec(name = "addweek", returnType = "time", formalParameters = { "time", "int", "string" })
	public static long addWeek(final long t, final long n, final String tz) {
		return BoaTimeIntrinsics.addPart(Calendar.DAY_OF_MONTH, t, n * 7, TimeZone.getTimeZone(tz));
	}

	/**
	 * Like addday, but for weeks.
	 * 
	 * @param t
	 *            A long representing the initial time
	 * 
	 * @param n
	 *            A long representing the number of weeks to add
	 * 
	 * @return A long representing the time with the specified number of weeks
	 *         added to it
	 */
	@FunctionSpec(name = "addweek", returnType = "time", formalParameters = { "time", "int" })
	public static long addWeek(final long t, final long n) {
		return BoaTimeIntrinsics.addPart(Calendar.DAY_OF_MONTH, t, n * 7, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * Like addday, but for weeks.
	 * 
	 * @param t
	 *            A long representing the initial time
	 * 
	 * @return A long representing the time with the specified number of weeks
	 *         added to it
	 */
	@FunctionSpec(name = "addweek", returnType = "time", formalParameters = { "time" })
	public static long addWeek(final long t) {
		return BoaTimeIntrinsics.addPart(Calendar.DAY_OF_MONTH, t, 7, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * Like addday, but for years.
	 * 
	 * @param t
	 *            A long representing the initial time
	 * 
	 * @param n
	 *            A long representing the number of years to add
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the time with the specified number of years
	 *         added to it
	 */
	@FunctionSpec(name = "addyear", returnType = "time", formalParameters = { "time", "int", "string" })
	public static long addYear(final long t, final long n, final String tz) {
		return BoaTimeIntrinsics.addPart(Calendar.YEAR, t, n, TimeZone.getTimeZone(tz));
	}

	/**
	 * Like addday, but for years.
	 * 
	 * @param t
	 *            A long representing the initial time
	 * 
	 * @param n
	 *            A long representing the number of years to add
	 * 
	 * @return A long representing the time with the specified number of years
	 *         added to it
	 */
	@FunctionSpec(name = "addyear", returnType = "time", formalParameters = { "time", "int" })
	public static long addYear(final long t, final long n) {
		return BoaTimeIntrinsics.addPart(Calendar.YEAR, t, n, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * Like addday, but for years.
	 * 
	 * @param t
	 *            A long representing the initial time
	 * 
	 * @return A long representing the time with the specified number of years
	 *         added to it
	 */
	@FunctionSpec(name = "addyear", returnType = "time", formalParameters = { "time" })
	public static long addYear(final long t) {
		return BoaTimeIntrinsics.addPart(Calendar.YEAR, t, 1, TimeZone.getTimeZone("PST8PDT"));
	}

	private static long partOf(final int which, final long t, final TimeZone tz) {
		final Calendar calendar = Calendar.getInstance(tz);

		calendar.setTimeInMillis(t / 1000);

		return calendar.get(which);
	}

	/**
	 * The numeric day of the month; for January 17, return 17, etc. An optional
	 * second argument, a string, names a timezone.
	 * 
	 * @param time
	 * 
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the numeric day of the month; for January 17,
	 *         return 17, etc
	 */
	@FunctionSpec(name = "dayofmonth", returnType = "int", formalParameters = { "time", "string" })
	public static long dayOfMonth(final long t, final String tz) {
		return BoaTimeIntrinsics.partOf(Calendar.DAY_OF_MONTH, t, TimeZone.getTimeZone(tz));
	}

	/**
	 * The numeric day of the month; for January 17, return 17, etc. An optional
	 * second argument, a string, names a timezone.
	 * 
	 * @param time
	 *            A long representing the time
	 * 
	 * @return A long representing the numeric day of the month; for January 17,
	 *         return 17, etc
	 */
	@FunctionSpec(name = "dayofmonth", returnType = "int", formalParameters = { "time" })
	public static long dayOfMonth(final long t) {
		return BoaTimeIntrinsics.partOf(Calendar.DAY_OF_MONTH, t, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * The numeric day of the week, from Monday=1 to Sunday=7 An optional second
	 * argument, a string, names a timezone.
	 * 
	 * @param t
	 * 
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the numeric day of the week, from Monday=1 to
	 *         Sunday=7
	 */
	@FunctionSpec(name = "dayofweek", returnType = "int", formalParameters = { "time", "string" })
	public static long dayOfWeek(final long t, final String tz) {
		return BoaTimeIntrinsics.partOf(Calendar.DAY_OF_WEEK, t, TimeZone.getTimeZone(tz));
	}

	/**
	 * The numeric day of the week, from Monday=1 to Sunday=7 An optional second
	 * argument, a string, names a timezone.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @return A long representing the numeric day of the week, from Monday=1 to
	 *         Sunday=7
	 */
	@FunctionSpec(name = "dayofweek", returnType = "int", formalParameters = { "time" })
	public static long dayOfWeek(final long t) {
		return BoaTimeIntrinsics.partOf(Calendar.DAY_OF_WEEK, t, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * The numeric day of the week, from Monday=1 to Sunday=7 An optional second
	 * argument, a string, names a timezone.
	 * 
	 * @param t
	 * 
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the numeric day of the year. January 1 is day
	 *         1
	 */
	@FunctionSpec(name = "dayofyear", returnType = "int", formalParameters = { "time", "string" })
	public static long dayOfYear(final long t, final String tz) {
		return BoaTimeIntrinsics.partOf(Calendar.DAY_OF_YEAR, t, TimeZone.getTimeZone(tz));
	}

	/**
	 * The numeric day of the year. January 1 is day 1. An optional second
	 * argument, a string, names a timezone.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @return A long representing the numeric day of the year. January 1 is day
	 *         1
	 */
	@FunctionSpec(name = "dayofyear", returnType = "int", formalParameters = { "time" })
	public static long dayOfYear(final long t) {
		return BoaTimeIntrinsics.partOf(Calendar.DAY_OF_YEAR, t, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * The numeric hour of the day, from 0 to 23. Midnight is 0, 1AM is 1, etc.
	 * An optional second argument, a string, names a timezone.
	 * 
	 * @param t
	 * 
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the numeric hour of the day, from 0 to 23.
	 *         Midnight is 0, 1AM is 1, etc
	 */
	@FunctionSpec(name = "hourof", returnType = "int", formalParameters = { "time", "string" })
	public static long hourOf(final long t, final String tz) {
		return BoaTimeIntrinsics.partOf(Calendar.HOUR_OF_DAY, t, TimeZone.getTimeZone(tz));
	}

	/**
	 * The numeric hour of the day, from 0 to 23. Midnight is 0, 1AM is 1, etc.
	 * An optional second argument, a string, names a timezone.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @return A long representing the numeric hour of the day, from 0 to 23.
	 *         Midnight is 0, 1AM is 1, etc
	 */
	@FunctionSpec(name = "hourof", returnType = "int", formalParameters = { "time" })
	public static long hourOf(final long t) {
		return BoaTimeIntrinsics.partOf(Calendar.HOUR_OF_DAY, t, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * The numeric minute of the hour, from 0 to 59. An optional second
	 * argument, a string, names a timezone.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the numeric minute of the hour, from 0 to 59
	 */
	@FunctionSpec(name = "minuteof", returnType = "int", formalParameters = { "time", "string" })
	public static long minuteOf(final long t, final String tz) {
		return BoaTimeIntrinsics.partOf(Calendar.MINUTE, t, TimeZone.getTimeZone(tz));
	}

	/**
	 * The numeric minute of the hour, from 0 to 59. An optional second
	 * argument, a string, names a timezone.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @return A long representing the numeric minute of the hour, from 0 to 59
	 */
	@FunctionSpec(name = "minuteof", returnType = "int", formalParameters = { "time" })
	public static long minuteOf(final long t) {
		return BoaTimeIntrinsics.partOf(Calendar.MINUTE, t, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * The numeric month of the year. January is 1. An optional second argument,
	 * a string, names a timezone.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the numeric month of the year. January is 1
	 */
	@FunctionSpec(name = "monthof", returnType = "int", formalParameters = { "time", "string" })
	public static long monthOf(final long t, final String tz) {
		return BoaTimeIntrinsics.partOf(Calendar.MONTH, t, TimeZone.getTimeZone(tz));
	}

	/**
	 * The numeric month of the year. January is 1. An optional second argument,
	 * a string, names a timezone.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @return A long representing the numeric month of the year. January is 1
	 */
	@FunctionSpec(name = "monthof", returnType = "int", formalParameters = { "time" })
	public static long monthOf(final long t) {
		return BoaTimeIntrinsics.partOf(Calendar.MONTH, t, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * The numeric year value, such as 2003. An optional second argument, a
	 * string, names a timezone.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the numeric year value, such as 2003
	 */
	@FunctionSpec(name = "secondof", returnType = "int", formalParameters = { "time", "string" })
	public static long secondOf(final long t, final String tz) {
		return BoaTimeIntrinsics.partOf(Calendar.SECOND, t, TimeZone.getTimeZone(tz));
	}

	/**
	 * The numeric year value, such as 2003. An optional second argument, a
	 * string, names a timezone.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @return A long representing the numeric year value, such as 2003
	 */
	@FunctionSpec(name = "secondof", returnType = "int", formalParameters = { "time" })
	public static long secondOf(final long t) {
		return BoaTimeIntrinsics.partOf(Calendar.SECOND, t, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * The numeric second of the minute, from 0 to 59. An optional second
	 * argument, a string, names a timezone.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the numeric second of the minute, from 0 to
	 *         59
	 */
	@FunctionSpec(name = "yearof", returnType = "int", formalParameters = { "time", "string" })
	public static long yearOf(final long t, final String tz) {
		return BoaTimeIntrinsics.partOf(Calendar.YEAR, t, TimeZone.getTimeZone(tz));
	}

	/**
	 * The numeric second of the minute, from 0 to 59. An optional second
	 * argument, a string, names a timezone.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @return A long representing the numeric second of the minute, from 0 to
	 *         59
	 */
	@FunctionSpec(name = "yearof", returnType = "int", formalParameters = { "time" })
	public static long yearOf(final long t) {
		return BoaTimeIntrinsics.partOf(Calendar.YEAR, t, TimeZone.getTimeZone("PST8PDT"));
	}

	private static long truncToDay(final long t, final TimeZone tz) {
		final Calendar calendar = Calendar.getInstance(tz);

		calendar.setTimeInMillis(t / 1000);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTimeInMillis() * 1000;
	}

	/**
	 * Truncate t to the zeroth microsecond of the day. Useful when creating
	 * variables indexed to a particular day, since all times in the day
	 * truncated with trunctoday will fold to the same value, which is the first
	 * time value in that day. An optional second argument, a string, names a
	 * timezone.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the time truncated to the zeroth microsecond
	 *         of the day
	 */
	@FunctionSpec(name = "trunctoday", returnType = "time", formalParameters = { "time", "string" })
	public static long truncToDay(final long t, final String tz) {
		return BoaTimeIntrinsics.truncToDay(t, TimeZone.getTimeZone(tz));
	}

	/**
	 * Truncate t to the zeroth microsecond of the day. Useful when creating
	 * variables indexed to a particular day, since all times in the day
	 * truncated with trunctoday will fold to the same value, which is the first
	 * time value in that day. An optional second argument, a string, names a
	 * timezone.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @return A long representing the time truncated to the zeroth microsecond
	 *         of the day
	 */
	@FunctionSpec(name = "trunctoday", returnType = "time", formalParameters = { "time" })
	public static long truncToDay(final long t) {
		return BoaTimeIntrinsics.truncToDay(t, TimeZone.getTimeZone("PST8PDT"));
	}

	private static long truncToHour(final long t, final TimeZone tz) {
		final Calendar calendar = Calendar.getInstance(tz);

		calendar.setTimeInMillis(t / 1000);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTimeInMillis() * 1000;
	}

	/**
	 * Like trunctoday, but truncate to the start of the hour.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the time truncated to the start of the hour
	 */
	@FunctionSpec(name = "trunctohour", returnType = "time", formalParameters = { "time", "string" })
	public static long truncToHour(final long t, final String tz) {
		return BoaTimeIntrinsics.truncToHour(t, TimeZone.getTimeZone(tz));
	}

	/**
	 * Like trunctoday, but truncate to the start of the hour.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @return A long representing the time truncated to the start of the hour
	 */
	@FunctionSpec(name = "trunctohour", returnType = "time", formalParameters = { "time" })
	public static long truncToHour(final long t) {
		return BoaTimeIntrinsics.truncToHour(t, TimeZone.getTimeZone("PST8PDT"));
	}

	private static long truncToMinute(final long t, final TimeZone tz) {
		final Calendar calendar = Calendar.getInstance(tz);

		calendar.setTimeInMillis(t / 1000);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTimeInMillis() * 1000;
	}

	/**
	 * Like trunctoday, but truncate to the start of the minute.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the time truncated to the start of the minute
	 */
	@FunctionSpec(name = "trunctominute", returnType = "time", formalParameters = { "time", "string" })
	public static long truncToMinute(final long t, final String tz) {
		return BoaTimeIntrinsics.truncToMinute(t, TimeZone.getTimeZone(tz));
	}

	/**
	 * Like trunctoday, but truncate to the start of the minute.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @return A long representing the time truncated to the start of the minute
	 */
	@FunctionSpec(name = "trunctominute", returnType = "time", formalParameters = { "time" })
	public static long truncToMinute(final long t) {
		return BoaTimeIntrinsics.truncToMinute(t, TimeZone.getTimeZone("PST8PDT"));
	}

	private static long truncToMonth(final long t, final TimeZone tz) {
		final Calendar calendar = Calendar.getInstance(tz);

		calendar.setTimeInMillis(t / 1000);
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTimeInMillis() * 1000;
	}

	/**
	 * Like trunctoday, but truncate to the start of the month.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the time truncated to the start of the month
	 */
	@FunctionSpec(name = "trunctomonth", returnType = "time", formalParameters = { "time", "string" })
	public static long truncToMonth(final long t, final String tz) {
		return BoaTimeIntrinsics.truncToMonth(t, TimeZone.getTimeZone(tz));
	}

	/**
	 * Like trunctoday, but truncate to the start of the month.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @return A long representing the time truncated to the start of the month
	 */
	@FunctionSpec(name = "trunctomonth", returnType = "time", formalParameters = { "time" })
	public static long truncToMonth(final long t) {
		return BoaTimeIntrinsics.truncToMonth(t, TimeZone.getTimeZone("PST8PDT"));
	}

	private static long truncToSecond(final long t, final TimeZone tz) {
		final Calendar calendar = Calendar.getInstance(tz);

		calendar.setTimeInMillis(t / 1000);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTimeInMillis() * 1000;
	}

	/**
	 * Like trunctoday, but truncate to the start of the second.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the time truncated to the start of the second
	 */
	@FunctionSpec(name = "trunctosecond", returnType = "time", formalParameters = { "time", "string" })
	public static long truncToSecond(final long t, final String tz) {
		return BoaTimeIntrinsics.truncToSecond(t, TimeZone.getTimeZone(tz));
	}

	/**
	 * Like trunctoday, but truncate to the start of the second.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @return A long representing the time truncated to the start of the second
	 */
	@FunctionSpec(name = "trunctosecond", returnType = "time", formalParameters = { "time" })
	public static long truncToSecond(final long t) {
		return BoaTimeIntrinsics.truncToSecond(t, TimeZone.getTimeZone("PST8PDT"));
	}

	private static long truncToYear(final long t, final TimeZone tz) {
		final Calendar calendar = Calendar.getInstance(tz);

		calendar.setTimeInMillis(t / 1000);
		calendar.set(Calendar.DAY_OF_YEAR, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTimeInMillis() * 1000;
	}

	/**
	 * Like trunctoday, but truncate to the start of the year.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A long representing the time truncated to the start of the year
	 */
	@FunctionSpec(name = "trunctoyear", returnType = "time", formalParameters = { "time", "string" })
	public static long truncToYear(final long t, final String tz) {
		return BoaTimeIntrinsics.truncToYear(t, TimeZone.getTimeZone(tz));
	}

	/**
	 * Like trunctoday, but truncate to the start of the year.
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @return A long representing the time truncated to the start of the year
	 */
	@FunctionSpec(name = "trunctoyear", returnType = "time", formalParameters = { "time" })
	public static long truncToYear(final long t) {
		return BoaTimeIntrinsics.truncToYear(t, TimeZone.getTimeZone("PST8PDT"));
	}

	/**
	 * Return the current time at the moment of execution. Note that the time
	 * value returned does not depend on a timezone.
	 * 
	 * @return A long representing the current time at the moment of execution.
	 */
	@FunctionSpec(name = "now", returnType = "time")
	public static long now() {
		return System.currentTimeMillis() * 1000;
	}

	private static String formatTime(final String formatstring, final long t, final TimeZone tz) {
		final Calendar calendar = Calendar.getInstance(tz);

		calendar.setTimeInMillis(t / 1000);

		final StringBuilder sb = new StringBuilder();

		boolean inEscape = false;
		for (final char c : formatstring.toCharArray())
			switch (c) {
			case '%':
				if (inEscape) {
					sb.append('%');
					inEscape = false;
				} else {
					inEscape = true;
				}
				break;
			default:
				if (inEscape) {
					if (BoaTimeIntrinsics.strftimeMap.containsKey(Character.valueOf(c))) {
						final SimpleDateFormat simpleDateFormat = BoaTimeIntrinsics.strftimeMap.get(Character.valueOf(c));
						simpleDateFormat.setTimeZone(tz);
						sb.append(simpleDateFormat.format(calendar.getTime()));
					} else {
						throw new RuntimeException("invalid escape string: %" + c);
					}
					inEscape = false;
				} else {
					sb.append(c);
				}
			}

		return sb.toString();
	}

	/**
	 * Return a string containing the time argument formatted according to the
	 * format string fmt. The syntax of the format string is the same as in ANSI
	 * C strftime. An optional third argument, a string, names a timezone.
	 * 
	 * @param formatstring
	 *            A {@link String} containing an ANSI C strftime format string
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @param tz
	 *            A {@link String} containing the name of the timezone to be
	 *            used
	 * 
	 * @return A string containing the time argument formatted according to the
	 *         format string fmt.
	 */
	@FunctionSpec(name = "formattime", returnType = "string", formalParameters = { "string", "time", "string" })
	public static String formatTime(final String formatstring, final long t, final String tz) {
		return BoaTimeIntrinsics.formatTime(formatstring, t, TimeZone.getTimeZone(tz));
	}

	/**
	 * Return a string containing the time argument formatted according to the
	 * format string fmt. The syntax of the format string is the same as in ANSI
	 * C strftime. An optional third argument, a string, names a timezone.
	 * 
	 * @param formatstring
	 *            A {@link String} containing an ANSI C strftime format string
	 * 
	 * @param t
	 *            A long representing the time
	 * 
	 * @return A string containing the time argument formatted according to the
	 *         format string fmt.
	 */
	@FunctionSpec(name = "formattime", returnType = "string", formalParameters = { "string", "time" })
	public static String formatTime(final String formatstring, final long t) {
		return BoaTimeIntrinsics.formatTime(formatstring, t, TimeZone.getTimeZone("PST8PDT"));
	}
}
