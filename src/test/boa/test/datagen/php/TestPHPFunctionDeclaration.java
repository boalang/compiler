package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPFunctionDeclaration extends PHPBaseTest {

	@Test
	public void testFunctionDeclaration() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/FunctionDeclarationNode.boa"),
				load("test/datagen/PHP/FunctionDeclarationNode.php"));
	}
	
	@Test
	public void testFunctionDeclarationWithFormalParamater() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/FormalParamaterNode.boa"),
				load("test/datagen/PHP/FormalParamaterNode.php"));
	}
	
	@Test
	public void testLambdaDeclaration() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/LambdaDeclarationNode.boa"),
				load("test/datagen/PHP/LambdaDeclarationNode.php"));
	}
	
	@Test
	public void testMethodDeclaration() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/MethodDeclarationNode.boa"),
				load("test/datagen/PHP/MethodDeclarationNode.php"));
	}
	
}
