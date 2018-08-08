package boa.test.datagen.queries;

import org.junit.Test;

public class TestAssert extends TestQueries {

	@Test
	public void testAssert() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v1/AssertDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v2/AssertDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v3/AssertDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v3/AssertDemo.java][1532028844000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v3/AssertDemo.java][1532032882000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v4/AssertDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v4/AssertDemo.java][1532029015000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v4/AssertDemo.java][1532032882000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/TEDemo/v1/TEDemo.java][1531520725000000] = 1\n";
		queryTest("test/known-good/assert.boa", expected);
	}
}
