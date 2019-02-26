package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPConstantDeclarationNode extends PHPBaseTest{

	@Test
	public void testConstantDeclarationUnderNamespace() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/ConstantDeclarationNode.boa"),
				load("test/datagen/PHP/ConstantDeclarationNode.php"));
	}
	
}
