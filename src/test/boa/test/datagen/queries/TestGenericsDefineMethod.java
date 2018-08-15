package boa.test.datagen.queries;

import org.junit.Test;

public class TestGenericsDefineMethod extends QueryTest {

	@Test
	public void testGenericsDefineMethod() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS2/GenDemo/v5/GenDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v5/GenDemo.java][1531532908000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v5/GenDemo5.java][1532699593000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v5/GenDemo5.java][1532699626000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BoxDemo/Box.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BoxDemo/Box.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo2.java][1532029613000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo2.java][1532029613000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo2.java][1532032882000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo2.java][1532032882000000] = 1\n";
		queryTest("test/known-good/generics-define-method.boa", expected);
	}
}
