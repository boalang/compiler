package boa.test.compiler;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestCodegen extends BaseTest {
	final private static String rootDir = "test/codegen/";


	@Test
	public void curry() throws IOException {
		codegen(load(rootDir + "curry.boa"));
	}
}
