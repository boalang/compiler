package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPForEachStatementNode extends PHPBaseTest {

	@Test
	public void forEachStatementNode() throws IOException, Exception {
		nodeTest(load("test/datagen/PHP/ForEachStatementNode.boa"), 
				load("test/datagen/PHP/ForEachStatementNode.php"));
	}
	
}
