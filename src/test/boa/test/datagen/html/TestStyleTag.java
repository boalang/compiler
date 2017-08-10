package boa.test.datagen.html;

import java.io.IOException;

import org.junit.Test;

public class TestStyleTag extends HTMLBaseTest {

	@Test
	public void testStyleTag() throws IOException, Exception{
		nodeTest(load("test/datagen/HTML/StyleTag.boa"),
				load("test/datagen/HTML/StyleTag.HTML"));
	}
	
}
