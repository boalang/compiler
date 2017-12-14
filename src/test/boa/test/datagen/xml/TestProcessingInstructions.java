package boa.test.datagen.xml;

import java.io.IOException;

import org.junit.Test;

public class TestProcessingInstructions extends XMLBaseTest {

	@Test
	public void testNameSpace() throws IOException, Exception{
		nodeTest(load("test/datagen/XML/ProcessingInstructions.boa"),
				("test/datagen/XML/ProcessingInstructions.XML"));
	}
	
}
