package boa.test.datagen.php;

import java.io.IOException;

import org.junit.Test;

public class TestPHPInlineHtml extends PHPBaseTest {

	@Test
	public void testInLineHtml() throws IOException, Exception{
		nodeTest(load("test/datagen/PHP/InLineHtml.boa"),
				load("test/datagen/PHP/InLineHtml.php"));
	}
	
	
}
