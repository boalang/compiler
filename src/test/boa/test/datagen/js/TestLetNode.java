package boa.test.datagen.js;

import java.io.IOException;

import org.junit.Test;

public class TestLetNode extends JavaScriptBaseTest {

	@Test
	public void letTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/LetNode.boa"), load("test/datagen/javascript/LetNode.js"));
	}

}
