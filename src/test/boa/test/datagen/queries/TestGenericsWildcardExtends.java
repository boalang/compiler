package boa.test.datagen.queries;

import org.junit.Test;

public class TestGenericsWildcardExtends extends QueryTest {

	@Test
	public void testGenericsWildcardExtends() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v2/GenDemo.java][1534438125000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/GenDemo/v2/GenDemo2.java][1534438125000000] = 1\n";
		queryTest("test/known-good/generics-wildcard-extends.boa", expected);
	}
}
