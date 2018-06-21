package boa.test.datagen.css;

import java.io.IOException;

import org.junit.Test;

public class TestStyleRule extends CssBaseTest{

	@Test
	public void testStyleRule() throws IOException, Exception{
		nodeTest(load("test/datagen/css/Style_Rule.boa"),
				load("test/datagen/css/Style_Rule.css"));
	}
	
	@Test
	public void testStyleRule1() throws IOException, Exception{
		nodeTest(load("test/datagen/css/Style_Rule1.boa"),
				load("test/datagen/css/Style_Rule1.css"));
	}
	
	@Test
	public void testStyleRule2() throws IOException, Exception{
		nodeTest(load("test/datagen/css/Style_Rule2.boa"),
				load("test/datagen/css/Style_Rule2.css"));
	}
	
	@Test
	public void testStyleRule3() throws IOException, Exception{
		nodeTest(load("test/datagen/css/Style_Rule3.boa"),
				load("test/datagen/css/Style_Rule3.css"));
	}
	
	@Test
	public void testMediaRule() throws IOException, Exception{
		nodeTest(load("test/datagen/css/Media_Rule.boa"),
				load("test/datagen/css/Media_Rule.css"));
	}
}
