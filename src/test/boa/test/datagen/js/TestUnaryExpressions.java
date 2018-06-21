package boa.test.datagen.js;

import java.io.IOException;

import org.junit.Test;

public class TestUnaryExpressions extends JavaScriptBaseTest {

	@Test
	public void testUnaryExpressionTYPEOF() throws IOException {
		nodeTest(load("test/datagen/javascript/UnaryExpressionTYPEOF.boa"), 
				load("test/datagen/javascript/UnaryExpressionTYPEOF.js"));
	}
	
	@Test
	public void testUnaryExpressionDELETE() throws IOException {
		nodeTest(load("test/datagen/javascript/UnaryExpressionDELETE.boa"), 
				load("test/datagen/javascript/UnaryExpressionDELETE.js"));
	}
	
	@Test
	public void testUnaryExpressionVOID() throws IOException {
		nodeTest(load("test/datagen/javascript/UnaryExpressionVOID.boa"), 
				load("test/datagen/javascript/UnaryExpressionVOID.js"));
	}
	
}
