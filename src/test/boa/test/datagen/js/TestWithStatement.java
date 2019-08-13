package boa.test.datagen.js;

import java.io.IOException;

import org.junit.Test;

public class TestWithStatement extends JavaScriptBaseTest {

	@Test
	public void withTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/WithNode.boa"), load("test/datagen/javascript/WithNode.js"));
	}

}
