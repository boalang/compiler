package boa.test.datagen.queries;

import org.junit.Test;

public class TestEnhancedFor extends QueryTest {

	@Test
	public void testEnhancedFor() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v4/AssertDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/EnForLoopDemo/v1/EnForLoopDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/EnForLoopDemo/v2/EnForLoopDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/TEDemo/v2/TEDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/VarargsDemo/VarargsDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/Touch/Touch.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo2.java][1532029613000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo2.java][1532029613000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo2.java][1532032882000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo2.java][1532032882000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v10/LambdaDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v4/LambdaDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v4/LambdaDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v4/LambdaDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v9/LambdaDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/MRDemo/v1/MRDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/MRDemo/v1/MRDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/RADemo/RADemo.java][1531520725000000] = 1\n";
		queryTest("test/known-good/enhanced-for.boa", expected);
	}
}
