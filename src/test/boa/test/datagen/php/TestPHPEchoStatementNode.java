package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPEchoStatementNode extends PHPBaseTest {

	@Test
	public void echoStatementNode() throws IOException, Exception {
		nodeTest(load("test/datagen/PHP/EchoStatementNode.boa"), 
				load("test/datagen/PHP/EchoStatementNode.php"));
	}

}
