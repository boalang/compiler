package boa.test.datagen.js;

import java.io.IOException;

import org.junit.Test;

public class TestParenthesizedExpression extends JavaScriptBaseTest {

	@Test
	public void testParanthesizedExpression() throws IOException {
		nodeTest(load("test/datagen/javascript/ParenthesizedExpression.boa"),
				 load("test/datagen/javascript/ParenthesizedExpression.js"));
	}
	
}
