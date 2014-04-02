package boa.functions;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class TestBoaSpecialIntrinsics {
	@Test
	public void testBoaSpecialIntrinsicsSaw() {
		final String[] result = BoaSpecialIntrinsics.saw("abcdef", "...", "..", ".");

		final String[] expected = new String[] { "abc", "de", "f" };

		Assert.assertEquals("result is the wrong size", expected.length, result.length);
		Assert.assertTrue("result is not equal", Arrays.equals(expected, result));
	}

	@Test
	public void testBoaSpecialIntrinsicsSaw2() {
		final String[] result = BoaSpecialIntrinsics.saw("abcdef", "abc", "de", "g");

		final String[] expected = new String[] { "abc", "de" };

		Assert.assertEquals("result is the wrong size", expected.length, result.length);
		Assert.assertTrue("result is not equal", Arrays.equals(expected, result));
	}

	@Test
	public void testBoaSpecialIntrinsicsSaw3D() {
		final String[] result = BoaSpecialIntrinsics.saw("abcdef", "abc", "e", "f");

		final String[] expected = new String[] { "abc", "e", "f" };

		Assert.assertEquals("result is the wrong size", expected.length, result.length);
		Assert.assertTrue("result is not equal", Arrays.equals(expected, result));
	}

	@Test
	public void testBoaSpecialIntrinsicsSaw4() {
		final String[] result = BoaSpecialIntrinsics.saw("abcdef", "abc", "^e", "f");

		final String[] expected = new String[] { "abc" };

		Assert.assertEquals("result is the wrong size", expected.length, result.length);
		Assert.assertTrue("result is not equal", Arrays.equals(expected, result));
	}

	@Test
	public void testBoaSpecialIntrinsicsSawzall() {
		final String[] result = BoaSpecialIntrinsics.sawzall("1	2	3	4	5	6	7	8", "[^\t]+");

		final String[] expected = new String[] { "1", "2", "3", "4", "5", "6", "7", "8" };

		Assert.assertEquals("result is the wrong size", expected.length, result.length);
		Assert.assertTrue("result is not equal", Arrays.equals(expected, result));
	}
}
