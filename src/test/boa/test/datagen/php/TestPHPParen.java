package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPParen extends PHPBaseTest {

	@Test
	public void testParen() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/Paren.boa"),
				load("test/datagen/PHP/Paren.php"));
	}
	
}
