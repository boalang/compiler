package boa.test.datagen.html;

import java.io.IOException;

import org.junit.Test;

public class TestHtmlTextAreaTag extends HTMLBaseTest {

	@Test
	public void testTextArea() throws IOException, Exception{
		nodeTest(load("test/datagen/HTML/TextAreaTag.boa"),
				load("test/datagen/HTML/TextAreaTag.HTML"));
	}
	
}
