package boa.test.datagen.queries;

import org.junit.Test;

public class TestGenericDefineType extends QueryTest{

	@Test
	public void testGenericsDefineType() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS2/GenDemo/v1/GenDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/GenDemo/v2/GenDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/GenDemo/v5/GenDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v1/GenDemo.java][1531532908000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v1/GenDemo1.java][1532699593000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v1/GenDemo1.java][1532699626000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v1/GenDemo1.java][1534433645000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v2/GenDemo.java][1531532908000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v2/GenDemo.java][1534438125000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v2/GenDemo2.java][1532699593000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v2/GenDemo2.java][1532699626000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v2/GenDemo2.java][1534438125000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v5/GenDemo.java][1531532908000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v5/GenDemo5.java][1532699593000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v5/GenDemo5.java][1532699626000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v5/GenDemo5.java][1534433645000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BoxDemo/Box.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BoxDemo/Box.java][1531880750000000] = 1\n";
		queryTest("test/known-good/generics-define-type.boa", expected);
	}
}
