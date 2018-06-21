package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPReflectionVariable extends PHPBaseTest {

	@Test
	public void testReflectionVariable() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/ReflectionVariable.boa"),
				load("test/datagen/PHP/ReflectionVariable.php"));
	}
	
}
