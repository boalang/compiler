package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPTraitNodes extends PHPBaseTest {

	@Test
	public void testTraitDeclarationNode() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/TraitDeclarationNode.boa"),
				load("test/datagen/PHP/TraitDeclarationNode.php"));
	}
	
	@Test
	public void testTraitUseStatementNode() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/TraitUseStatementNode.boa"),
				load("test/datagen/PHP/TraitUseStatementNode.php"));
	}
	
	@Test
	public void testTraitPrecedenceNodes() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/TraitPrecedenceNodes.boa"),
				load("test/datagen/PHP/TraitPrecedenceNodes.php"));
	}
	
	
	@Test
	public void testTraitAliasNodes() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/TraitAliasNodes.boa"),
				load("test/datagen/PHP/TraitAliasNodes.php"));
	}
	
	@Test
	public void testFullyQualifiedTraitMethodReference() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/TraitAliasWithFullyQualifiedTraitMethodReference.boa"),
				load("test/datagen/PHP/TraitAliasWithFullyQualifiedTraitMethodReference.php"));
	}
	
}
