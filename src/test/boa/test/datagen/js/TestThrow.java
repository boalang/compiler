package boa.test.datagen.js;

import java.io.IOException;

import org.junit.Test;

public class TestThrow extends JavaScriptBaseTest {

	@Test
	public void throwTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/ThrowNode.boa"), load("test/datagen/javascript/ThrowNode.js"));
	}

	
}
