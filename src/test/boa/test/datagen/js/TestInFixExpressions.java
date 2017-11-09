package boa.test.datagen.js;

import java.io.IOException;

import org.junit.Test;

public class TestInFixExpressions extends JavaScriptBaseTest {

	@Test
	public void testInFixExpressionSHEQ() throws IOException {
		nodeTest(load("test/datagen/javascript/InFixExpressionSHEQ.boa"), 
				load("test/datagen/javascript/InFixExpressionSHEQ.js"));
	}
	
	@Test
	public void testInFixExpressionSHNEQ() throws IOException {
		nodeTest(load("test/datagen/javascript/InFixExpressionSHNEQ.boa"), 
				load("test/datagen/javascript/InFixExpressionSHNEQ.js"));
	}
	
	@Test
	public void testInFixExpressionIN() throws IOException {
		nodeTest(load("test/datagen/javascript/InFixExpressionIN.boa"), 
				load("test/datagen/javascript/InFixExpressionIN.js"));
	}
	
	@Test
	public void testInFixExpressionTYPECOMPARE() throws IOException {
		nodeTest(load("test/datagen/javascript/InFixExpressionTYPECOMPARE.boa"), 
				load("test/datagen/javascript/InFixExpressionTYPECOMPARE.js"));
	}
	
	
}
