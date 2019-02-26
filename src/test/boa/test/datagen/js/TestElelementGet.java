package boa.test.datagen.js;

import java.io.IOException;

import org.junit.Test;

public class TestElelementGet extends JavaScriptBaseTest {

	@Test
	public void elementGetTest1() throws IOException{
		nodeTest( load("test/datagen/javascript/ElementGetNode.boa"), load("test/datagen/javascript/ElementGetNode.js"));
	}
	
}
