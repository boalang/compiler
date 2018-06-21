package boa.test.datagen.html;

import java.io.IOException;

import org.junit.Test;

public class TestHtmlWithEmbeddedPHP extends HTMLBaseTest {

	@Test
	public void testEmbeddedPHP() throws IOException, Exception{
		nodeTest(load("test/datagen/HTML/EmbeddedPHP.boa"),
				load("test/datagen/HTML/EmbeddedPHP.HTML"));
	}
	
}
