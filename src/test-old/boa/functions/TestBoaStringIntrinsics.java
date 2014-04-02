package boa.functions;

import java.util.Arrays;

import org.junit.Test;

import junit.framework.Assert;

public class TestBoaStringIntrinsics {
	@Test
	public void testBoaStringIntrinsicsSplitCsvLine() {
		final byte[][] splitCsvLine = BoaStringIntrinsics.splitCsvLine("abc,1".getBytes());

		final byte[][] expected = new byte[2][];
		expected[0] = "abc".getBytes();
		expected[1] = "1".getBytes();

		Assert.assertEquals("length is incorrect", 2, splitCsvLine.length);
		Assert.assertTrue("not equal", Arrays.equals(expected[0], splitCsvLine[0]));
		Assert.assertTrue("not equal", Arrays.equals(expected[1], splitCsvLine[1]));
	}
}
