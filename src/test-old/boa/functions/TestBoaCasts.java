package boa.functions;

import java.text.ParseException;

import junit.framework.Assert;

import org.junit.Test;

public class TestBoaCasts {
	@Test
	public void testBoaCastsStringToTime() throws ParseException {
		Assert.assertEquals("stringToTime is wrong", 1044258008000000l, BoaCasts.stringToTime("Sun Feb  2 23:40:08 PST 2003"));
	}
}
