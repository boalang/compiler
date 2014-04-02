package boa.functions;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;

import junit.framework.Assert;

public class TestBoaTimeIntrinsics {
	@Test
	public void testBoaTimeIntrinsicsAddDay() {
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("PST8PDT"));
		calendar.set(2001, 1, 28, 0, 0, 0);
		final long t = calendar.getTimeInMillis() * 1000;

		Assert.assertEquals("addDay is wrong", "Thu Mar 1 00:00:00 2001", BoaTimeIntrinsics.formatTime("%c", BoaTimeIntrinsics.addDay(t, 1)));
	}

	@Test
	public void testBoaTimeIntrinsicsDayOfMonth() {
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("PST8PDT"));
		calendar.set(2001, 1, 28, 0, 0, 0);
		final long t = calendar.getTimeInMillis() * 1000;

		Assert.assertEquals("dayOfMonth is wrong", 28, BoaTimeIntrinsics.dayOfMonth(t));
	}

	@Test
	public void testBoaTimeIntrinsicsTruncateToDay() {
		Assert.assertEquals("truncateToDay is wrong", "Fri Feb 13 00:00:00 2009",
				BoaTimeIntrinsics.formatTime("%c", BoaTimeIntrinsics.truncToDay(1234567890000000l)));
	}

	@Test
	public void testBoaTimeIntrinsicsFormatTime() {
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("PST8PDT"));
		calendar.set(2001, 1, 28, 0, 0, 0);
		final long t = calendar.getTimeInMillis() * 1000;

		Assert.assertEquals("formatTime is wrong", "% That was Wed Feb 28 00:00:00 2001 in timezone PST %",
				BoaTimeIntrinsics.formatTime("%% That was %c in timezone %Z %%", t));
	}
}
