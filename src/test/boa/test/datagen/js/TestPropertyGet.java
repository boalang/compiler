package boa.test.datagen.js;

import java.io.IOException;

import org.junit.Test;

public class TestPropertyGet extends JavaScriptBaseTest {

	@Test
	public void propertyGetTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/PropertyGetNode.boa"),
				load("test/datagen/javascript/PropertyGetNode.js"));
	}

}
