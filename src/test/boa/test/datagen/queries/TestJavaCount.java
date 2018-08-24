package boa.test.datagen.queries;

import org.junit.Test;

public class TestJavaCount extends QueryTest {

	@Test
	public void testJavaCount() {
		String expected = "counts[] = 62\n";
		queryTest("test/known-good/java-count.boa", expected);
	}
}
