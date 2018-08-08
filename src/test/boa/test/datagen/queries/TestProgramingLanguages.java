package boa.test.datagen.queries;

import org.junit.Test;

public class TestProgramingLanguages extends QueryTest {

	@Test
	public void testProgramingLanguages() {
		String expected = "counts[] = Java, 1.0\n";
		queryTest("test/known-good/catAp1.boa", expected);
	}
}
