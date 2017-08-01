package boa.test.datagen;

import java.io.IOException;

import org.junit.Test;

public class TestFucntionNode extends JavaScriptBaseTest {

	@Test
	public void functionTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/FunctionNode.boa"), load("test/datagen/javascript/FunctionNode.js"));
	}

	@Test
	public void functionTest2() throws IOException {
		nodeTest(load("test/datagen/javascript/FunctionNode2.boa"), load("test/datagen/javascript/FunctionNode2.js"));
	}

	@Test
	public void functionTest3() throws IOException {
		nodeTest(load("test/datagen/javascript/FunctionNode3.boa"), load("test/datagen/javascript/FunctionNode3.js"));
	}
}
