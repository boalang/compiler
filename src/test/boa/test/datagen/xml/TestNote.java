package boa.test.datagen.xml;

import java.io.IOException;

import org.junit.Test;

public class TestNote extends XMLBaseTest {

	@Test
	public void testNameSpace() throws IOException, Exception{
		nodeTest(load("test/datagen/XML/Note.boa"),
				("test/datagen/XML/Note.XML"));
	}
	
}
