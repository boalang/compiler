package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPUseStatement extends PHPBaseTest{

	@Test
	public void testUseStatement() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/UseStatementNode.boa"),
				load("test/datagen/PHP/UseStatementNode.php"));
	}
	
	@Test
	public void testUseStatementWithAlias() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/UseStatementWithAlias.boa"),
				load("test/datagen/PHP/UseStatementWithAlias.php"));
	}
	
}
