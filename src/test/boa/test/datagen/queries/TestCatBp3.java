package boa.test.datagen.queries;

import org.junit.Test;

public class TestCatBp3 extends QueryTest {

	@Test
	public void testcatBp18() {
		String expected = "counts[] = 2\n";
		queryTest("test/known-good/catBp3.boa", expected);
	}
}
