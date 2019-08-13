package boa.test.datagen.queries;

import org.junit.Test;

public class TestCatAp4 extends QueryTest {

	@Test
	public void testCatAp4() {
		String expected = "counts[2018] = 1\n";
		queryTest("test/known-good/catAp4.boa", expected);
	}
}
