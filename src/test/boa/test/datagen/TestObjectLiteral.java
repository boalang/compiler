package boa.test.datagen;

import java.io.IOException;

import org.junit.Test;

public class TestObjectLiteral extends JavaScriptBaseTest {

	@Test
	public void objectLiteralTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/ObjectLiteralNode.boa"),
				load("test/datagen/javascript/ObjectLiteralNode.js"));
	}

	@Test
	public void objectLiteralTest2() throws IOException {
		nodeTest(load("test/datagen/javascript/ObjectLiteralNode2.boa"),
				load("test/datagen/javascript/ObjectLiteralNode2.js"));
	}
}
