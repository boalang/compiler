package boa.test.datagen;

import java.io.IOException;

import org.junit.Test;

public class TestXml extends JavaScriptBaseTest {

	@Test
	public void xmlLiteralTest() throws IOException {
		nodeTest(load("test/datagen/javascript/XmlLiteralNode.boa"), load("test/datagen/javascript/XmlLiteralNode.js"));
	}
	
	@Test
	public void xmlDotQueryTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/XmlDotQueryNode.boa"), load("test/datagen/javascript/XmlDotQueryNode.js"));
	}
	
	@Test
	public void xmlPropRefTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/XmlPropRefNode.boa"), load("test/datagen/javascript/XmlPropRefNode.js"));
	}
	
}
