package boa.test.datagen.html;

import java.io.IOException;

import org.junit.Test;

public class TestXmlParse extends HTMLBaseTest {

	@Test
	public void testXml() throws IOException, Exception{
		nodeTest(load("test/datagen/HTML/xml.boa"),
				load("test/datagen/HTML/xml.xml"));
	}
	
}
