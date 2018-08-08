package boa.test.datagen.queries;

import org.junit.Test;

public class TestYearJavaAdded extends QueryTest {

	@Test
	public void testYearJavaAdded() {
		String expected = "counts[2018] = 1\n";
		queryTest("test/known-good/catAp4.boa", expected);
	}
}
