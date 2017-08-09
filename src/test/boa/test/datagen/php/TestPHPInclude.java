package boa.test.datagen;

import java.io.IOException;

import org.junit.Test;

public class TestPHPInclude extends PHPBaseTest {

	@Test
	public void testInclude() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/Include.boa"),
				load("test/datagen/PHP/Include.php"));
	}
	
}
