package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPQuote extends PHPBaseTest {

	@Test
	public void testQuote() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/QuoteNode.boa"),
				load("test/datagen/PHP/QuoteNode.php"));
	}
	
}
