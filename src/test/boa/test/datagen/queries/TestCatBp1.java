package boa.test.datagen.queries;

import org.junit.Test;

public class TestCatBp1 extends QueryTest {

	@Test
	public void testCatBp1() {
		String expected = "counts[] = 53\n";
		queryTest("test/known-good/catBp1.boa", expected);
	}
}
