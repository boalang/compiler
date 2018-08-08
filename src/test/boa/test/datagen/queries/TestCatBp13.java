package boa.test.datagen.queries;

import org.junit.Test;

public class TestCatBp13 extends TestQueries {
	
	@Test
	public void testcatBp13() {
		String expected = "counts[2018] = 1\n";
		queryTest("test/known-good/catBp13.boa", expected);
	}
}
