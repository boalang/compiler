package boa.test.datagen.html;

import java.io.IOException;

import org.junit.Test;

public class TestHTMLPitcureTag extends HTMLBaseTest {

	@Test
	public void testPictureTag() throws IOException, Exception{
		nodeTest(load("test/datagen/HTML/PictureTag.boa"),
				load("test/datagen/HTML/PictureTag.HTML"));
	}
	
}
