package boa.test.datagen;

import java.io.IOException;

import org.junit.Test;

public class TestForLoops extends JavaScriptBaseTest{

	// The loop body returned a ScoopNode
	@Test
	public void forInLoopTest1() throws IOException{
		nodeTest( load("test/datagen/javascript/ForInLoopNode.boa"), load("test/datagen/javascript/ForInLoopNode.js"));
	}
	
	// The loop body returned a ScoopNode
	@Test
	public void forLoopTest1() throws IOException{
		nodeTest( load("test/datagen/javascript/ForLoopNode.boa"), load("test/datagen/javascript/ForLoopNode.js"));
	}
	
	// The loop body returned a ScoopNode
		@Test
		public void forLoopBreakTest1() throws IOException{
			nodeTest( load("test/datagen/javascript/ForLoopWithBreakStatementNode.boa"), load("test/datagen/javascript/ForLoopWithBreakStatementNode.js"));
		}
	
}
