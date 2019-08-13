package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPCloneExpressionNode extends PHPBaseTest {

	@Test
	public void cloneExpressionNode() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/CloneExpressionNode.boa"),
				load("test/datagen/PHP/CloneExpressionNode.php"));
	}
	
}
