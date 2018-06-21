package boa.test.datagen.js;

import java.io.IOException;

import org.junit.Test;

public class TestJavaScriptKeyWordLiteral extends JavaScriptBaseTest{

	@Test
	public void thisKeyWordLiteralTest() throws IOException {
		nodeTest(load("test/datagen/javascript/ThisKeyWordLiteralNode.boa"),
				load("test/datagen/javascript/ThisKeyWordLiteralNode.js"));
	}
	
	@Test
	public void trueFalseKeyWordLiteralTest() throws IOException {
		nodeTest(load("test/datagen/javascript/TrueFalseKeyWordLiteralNodes.boa"),
				load("test/datagen/javascript/TrueFalseKeyWordLiteralNodes.js"));
	}
	
	@Test
	public void debuggerKeyWordLiteralTest() throws IOException {
		nodeTest(load("test/datagen/javascript/DebuggerKeyWordLiteralNode.boa"),
				load("test/datagen/javascript/DebuggerKeyWordLiteralNode.js"));
	}
	
}
