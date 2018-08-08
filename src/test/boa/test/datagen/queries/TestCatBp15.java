package boa.test.datagen.queries;

import org.junit.Test;

public class TestCatBp15 extends TestQueries {
	
	@Test
	public void testcatBp15() {
		String expected = "counts[140492550] = 5.530612244897959\n";
		queryTest("test/known-good/catBp15.boa", expected);
	}
}
