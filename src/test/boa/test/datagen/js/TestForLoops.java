package boa.test.datagen.js;

import java.io.IOException;

import org.junit.Test;

public class TestForLoops extends JavaScriptBaseTest {

	@Test
	public void forInLoopTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/ForInLoopNode.boa"),
				load("test/datagen/javascript/ForInLoopNode.js"));
	}
	
	@Test
	public void forEachLoopTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/ForEachNode.boa"), 
				load("test/datagen/javascript/ForEachNode.js"));
	}

	@Test
	public void forLoopTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/ForLoopNode.boa"),
				load("test/datagen/javascript/ForLoopNode.js"));
	}

	@Test
	public void forLoopBreakTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/ForLoopWithBreakStatementNode.boa"),
				load("test/datagen/javascript/ForLoopWithBreakStatementNode.js"));
	}

	@Test
	public void forLoopContinueTest1() throws IOException {
		nodeTest(load("test/datagen/javascript/ContinueStatementNode.boa"),
				load("test/datagen/javascript/ContinueStatementNode.js"));
	}
}
