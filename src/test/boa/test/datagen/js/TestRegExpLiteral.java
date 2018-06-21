package boa.test.datagen.js;

import java.io.IOException;

import org.junit.Test;

public class TestRegExpLiteral extends JavaScriptBaseTest {

	@Test
	public void testRegExpLiteral() throws IOException {
		nodeTest(load("test/datagen/javascript/RegExpLiteral.boa"),
				load("test/datagen/javascript/RegExpLiteral.js"));
	}
	
}
