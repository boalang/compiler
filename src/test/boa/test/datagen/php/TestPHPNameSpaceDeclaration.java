package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPNameSpaceDeclaration extends PHPBaseTest {
	
	@Test
	public void testNamespaceDeclaration() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/NamespaceDeclarationNode.boa"),
				 load("test/datagen/PHP/NamespaceDeclarationNode.php"));
	}
	
	@Test
	public void testNamespaceDeclaration2() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/NamespaceDeclarationNode2.boa"),
				 load("test/datagen/PHP/NamespaceDeclarationNode2.php"));
	}
	
	@Test
	public void testNestedNamespaceDeclarations() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/NestedNamespaces.boa"),
				 load("test/datagen/PHP/NestedNamespaces.php"));
	}
	
}
