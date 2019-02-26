package boa.test.datagen.queries;

import org.junit.Test;

public class TestGenericsDefineField extends QueryTest {

	@Test
	public void testGenericsDefineField() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS4/Planets/Planets.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/Planets/Planets.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/IGTIDemo/IGTIDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v10/LambdaDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v9/LambdaDemo.java][1531520725000000] = 1\n";
		queryTest("test/known-good/generics-define-field.boa", expected);
	}
}
