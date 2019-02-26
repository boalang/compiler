package boa.test.datagen.xml;

import java.io.IOException;

import org.junit.Test;

public class TestXMLNamespace extends XMLBaseTest{

	@Test
	public void testNameSpace() throws IOException, Exception{
		nodeTest(load("test/datagen/XML/Namespace.boa"),
				("test/datagen/XML/Namespace.XML"));
	}
	
}
