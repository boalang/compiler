package boa.test.datagen.queries;

import org.junit.Test;

public class TestCatAp3 extends QueryTest {

	@Test
	public void testCatAp3() {
		String expected = "counts[2018] = 1\n";
		queryTest("test/known-good/catAp3.boa", expected);
	}
}
