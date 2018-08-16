package boa.test.datagen.queries;

import org.junit.Test;

public class TestCatBp14 extends QueryTest {
	
	@Test
	public void testCatBp14() {
		String expected = "counts[] = 5.530612244897959\n";
		queryTest("test/known-good/catBp14.boa", expected);
	}
	
}
