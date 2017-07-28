package boa.test.datagen;

import java.io.IOException;

import org.junit.Test;

public class TestYield extends JavaScriptBaseTest {

	@Test
	public void yieldTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/YieldNode.boa"), load("test/datagen/javascript/YieldNode.js"));
	}
}
