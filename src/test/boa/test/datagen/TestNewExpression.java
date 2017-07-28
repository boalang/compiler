package boa.test.datagen;

import java.io.IOException;

import org.junit.Test;

public class TestNewExpression extends JavaScriptBaseTest {

	@Test
	public void newExpressionTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/NewExpressionNode.boa"),
				load("test/datagen/javascript/NewExpressionNode.js"));
	}

}
