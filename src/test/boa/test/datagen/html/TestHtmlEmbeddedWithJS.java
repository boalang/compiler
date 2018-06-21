package boa.test.datagen.html;

import java.io.IOException;

import org.junit.Test;

public class TestHtmlEmbeddedWithJS extends HTMLBaseTest {

	@Test
	public void testEmbeddedJS() throws IOException, Exception{
		nodeTest(load("test/datagen/HTML/EmbeddedJavaScript.boa"),
				load("test/datagen/HTML/EmbeddedJavaScript.HTML"));
	}
	
	@Test
	public void testExternalJS() throws IOException, Exception{
		nodeTest(load("test/datagen/HTML/ExternalScript.boa"),
				load("test/datagen/HTML/ExternalScript.HTML"));
	}
	
}
