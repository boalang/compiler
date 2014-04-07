package boa.test.compiler;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestTypecheck extends BaseTest {
	final private static String rootDir = "test/typecheck/";
	final private static String badDir = rootDir + "errors/";


	@Test
	public void cout() throws IOException {
		typecheck(load(badDir + "cout.boa"), "type 'string' does not support the '<<' operator");
	}

	@Test
	public void assignTypeToVar() throws IOException {
		typecheck(load(badDir + "assign-type-to-var.boa"), "type 'Project' is not a value and can not be assigned");
	}

	@Test
	public void assignTypeToVar2() throws IOException {
		typecheck(load(badDir + "assign-type-to-var2.boa"), "type 'Project' is not a value and can not be assigned");
	}

	@Test
	public void varAsType() throws IOException {
		typecheck(load(badDir + "var-as-type.boa"), "type 'input' undefined");
	}
}
