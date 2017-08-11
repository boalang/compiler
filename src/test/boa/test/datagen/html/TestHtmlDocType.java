package boa.test.datagen.html;

import java.io.IOException;

import org.junit.Test;

public class TestHtmlDocType extends HTMLBaseTest {

	@Test
	public void testDocType() throws IOException, Exception{
		nodeTest(load("test/datagen/HTML/DocType.boa"),
				load("test/datagen/HTML/DocType.HTML"));
	}
	
}
