package boa.test.datagen;

import java.io.IOException;

import org.junit.Test;

public class TestLabeledStatement extends JavaScriptBaseTest {

	@Test
	public void labeledStatementTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/LabeledStatementNode.boa"), load("test/datagen/javascript/LabeledStatementNode.js"));
	}
	
	@Test
	public void labeledStatementTest2() throws IOException {
		nodeTest(load("test/datagen/javascript/LabeledStatementNode2.boa"), load("test/datagen/javascript/LabeledStatementNode2.js"));
	}
	
}
