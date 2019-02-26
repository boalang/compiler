package boa.test.datagen.queries;

import org.junit.Test;

public class TestEnums extends QueryTest {
	
	@Test
	public void testTestEnums() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS3/TEDemo/v1/TEDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/TEDemo/v2/TEDemo.java][1531520725000000] = 1\n";
		queryTest("test/known-good/enums.boa", expected);
	}
}
