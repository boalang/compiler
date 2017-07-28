package boa.test.datagen;

import java.io.IOException;

import org.junit.Test;

public class TestDoLoop extends JavaScriptBaseTest {

	// The loop body returned a ScoopNode
		@Test
		public void DoLoopTest1() throws IOException{
			nodeTest( load("test/datagen/javascript/DoLoopNode.boa"), load("test/datagen/javascript/DoLoopNode.js"));
		}
	
}
