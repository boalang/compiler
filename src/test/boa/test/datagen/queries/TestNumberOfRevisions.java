package boa.test.datagen.queries;

import org.junit.Test;

public class TestNumberOfRevisions extends TestQueries {

	@Test
	public void testNumberOfRevisions() {
		String expected = "counts[] = 49\n";
		queryTest("test/known-good/catBp1.boa", expected);
	}
}
