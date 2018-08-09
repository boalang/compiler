package boa.test.datagen.queries;

import org.junit.Test;

public class TestDiamond extends QueryTest {
	
	@Test
	public void testDiamond() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS4/BoxDemo/BoxDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/IGTIDemo/IGTIDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v10/LambdaDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v9/LambdaDemo.java][1531520725000000] = 1\n";
		queryTest("test/known-good/diamond.boa", expected);
	}
}
