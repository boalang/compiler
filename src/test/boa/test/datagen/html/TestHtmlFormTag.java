package boa.test.datagen.html;

import java.io.IOException;

import org.junit.Test;

public class TestHtmlFormTag extends HTMLBaseTest {

	@Test
	public void testFormTag() throws IOException, Exception{
		nodeTest(load("test/datagen/HTML/FormTag.boa"),
				load("test/datagen/HTML/FormTag.HTML"));
	}
	
}
