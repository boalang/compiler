package boa.test.datagen.queries;

import org.junit.Test;

public class TestChurnRate extends QueryTest {
	
	@Test
	public void testChurnRate() {
		String expected = "counts[] = 5.530612244897959\n";
		queryTest("test/known-good/catBp14.boa", expected);
	}
	
}
