package boa.test.datagen.queries;

import org.junit.Test;

public class TestCatBp15 extends QueryTest {
	
	@Test
	public void testcatBp15() {
		String expected = "counts[140492550] = 6.018867924528302\n";
		queryTest("test/known-good/catBp15.boa", expected);
	}
}
