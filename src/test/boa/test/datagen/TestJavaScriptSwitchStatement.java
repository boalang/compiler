package boa.test.datagen;

import java.io.IOException;

import org.junit.Test;

public class TestJavaScriptSwitchStatement extends JavaScriptBaseTest {

	@Test
	public void switchStatementTest() throws IOException {
		nodeTest(load("test/datagen/javascript/SwitchStatementNode.boa"),
				load("test/datagen/javascript/SwitchStatementNode.js"));
	}
	
}
