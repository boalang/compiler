package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPGlobalStatementNode extends PHPBaseTest {

	@Test
	public void globalStatementNode() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/GlobalStatementNode.boa"),
				load("test/datagen/PHP/GlobalStatementNode.php"));
	}
	
}
