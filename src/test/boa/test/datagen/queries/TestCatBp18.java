package boa.test.datagen.queries;

import org.junit.Test;

public class TestCatBp18 extends QueryTest {
	
	@Test
	public void testcatBp18() {
		String expected = "counts[] = 4.3584905660377355\n";
		queryTest("test/known-good/catBp18.boa", expected);
	}
}
