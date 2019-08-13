package boa.test.datagen.queries;

import org.junit.Test;

public class TestGenericsWildcard extends QueryTest {

	@Test
	public void testGenericsWildcard() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS2/GenDemo/v4/GenDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/GenDemo/v4/GenDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v4/GenDemo.java][1531532908000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v4/GenDemo.java][1531532908000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v4/GenDemo.java][1532629853000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v4/GenDemo.java][1532629853000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v4/GenDemo.java][1532629997000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v4/GenDemo.java][1532629997000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v4/GenDemo4.java][1532699593000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v4/GenDemo4.java][1532699593000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v4/GenDemo4.java][1532699626000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v4/GenDemo4.java][1532699626000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v4/GenDemo4.java][1534433645000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v4/GenDemo4.java][1534433645000000] = 1\n";
		queryTest("test/known-good/generics-wildcard.boa", expected);
	}
}
