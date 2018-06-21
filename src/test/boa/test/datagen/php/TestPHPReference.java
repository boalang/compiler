package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPReference extends PHPBaseTest {

	@Test
	public void testReference() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/Reference.boa"),
				load("test/datagen/PHP/Reference.php"));
	}
	
}
