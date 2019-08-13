package boa.test.datagen.queries;

import org.junit.Test;

public class TestDeadCode extends QueryTest {
	
	@Test
	public void testDeadCode() {
		String expected = "DEAD[] = {\n"
				+ "   \"change\": \"UNKNOWN\",\n"
				+ "   \"kind\": \"SOURCE_JAVA_JLS4\",\n"
				+ "   \"name\": \"src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo.java\",\n"
				+ "   \"key\": 142653,\n"
				+ "   \"ast\": true\n"
				+ "} - unsafe, 1.0\n";
		queryTest("test/known-good/dead-code.boa", expected);
	}
}
