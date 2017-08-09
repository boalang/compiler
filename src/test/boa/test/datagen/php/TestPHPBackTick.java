package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPBackTick extends PHPBaseTest {

	@Test
	public void testCBackTick() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/BackTickNode.boa"),
				load("test/datagen/PHP/BackTickNode.php"));
	}
	
}
