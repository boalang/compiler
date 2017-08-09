package boa.test.datagen;

import java.io.IOException;

import org.junit.Test;

public class TestPHPStaticStatement extends PHPBaseTest {

	@Test
	public void testStaticStatement() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/StaticStatementNode.boa"),
				load("test/datagen/PHP/StaticStatementNode.php"));
	}
	
}
