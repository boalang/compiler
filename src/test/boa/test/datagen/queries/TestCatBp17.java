package boa.test.datagen.queries;

import org.junit.Test;

public class TestCatBp17 extends TestQueries {
	
	@Test
	public void testcatBp17() {
		String expected = "counts[2018] = 49\n";
		queryTest("test/known-good/catBp17.boa", expected);
	}
}
