package boa.test.datagen.queries;

import org.junit.Test;

public class TestMulticatch extends QueryTest {

	@Test
	public void testMulticatch() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS4/CopyToDatabaseOrFile/v2/CopyToDatabaseOrFile.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/CopyToDatabaseOrFile/v2/CopyToDatabaseOrFile.java][1531546584000000] = 1\n";
		queryTest("test/known-good/multicatch.boa", expected);
	}
}
