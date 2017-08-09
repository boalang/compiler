package boa.test.datagen.js;

import java.io.IOException;

import org.junit.Test;

public class TestTryCatch extends JavaScriptBaseTest {

	@Test
	public void TryCatchTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/TryCatchNodes.boa"), load("test/datagen/javascript/TryCatchNodes.js"));
	}

}
