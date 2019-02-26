package boa.test.datagen.queries;

import org.junit.Test;

public class TestCatBp14 extends QueryTest {
	
	@Test
	public void testCatBp14() {
		String expected = "counts[] = 6.018867924528302\n";
		queryTest("test/known-good/catBp14.boa", expected);
	}
	
}
