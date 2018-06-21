package boa.test.datagen.js;

import java.io.IOException;

import org.junit.Test;

public class TestWhileLoop extends JavaScriptBaseTest {

	// The loop body returned a ScoopNode
	@Test
	public void whileLoopTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/WhileNode.boa"), load("test/datagen/javascript/WhileNode.js"));
	}

}
