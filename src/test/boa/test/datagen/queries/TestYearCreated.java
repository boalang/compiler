package boa.test.datagen.queries;

import org.junit.Test;

public class TestYearCreated extends TestQueries {

	@Test
	public void testYearCreated() {
		String expected = "counts[2018] = 1\n";
		queryTest("test/known-good/catAp3.boa", expected);
	}
}
