package boa.test.datagen;

import java.io.IOException;

import org.junit.Test;

public class TestPHPGoto extends PHPBaseTest {

	@Test
	public void testGoto() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/GotoNodes.boa"),
				load("test/datagen/PHP/GotoNodes.php"));
	}
	
}
