package boa.test.datagen.queries;

import org.junit.Test;

public class TestCatAp1 extends QueryTest {

	@Test
	public void testCatAp1() {
		String expected = "counts[] = Java, 1.0\n";
		queryTest("test/known-good/catAp1.boa", expected);
	}
}
