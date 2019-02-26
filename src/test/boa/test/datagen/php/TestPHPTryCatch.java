package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPTryCatch extends PHPBaseTest {

	@Test
	public void testTryCatch() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/TryCatch.boa"),
				load("test/datagen/PHP/TryCatch.php"));
	}
	
}
