package boa.test.compiler;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestParser extends BaseTest {
	final private static String rootDir = "test/parsing/";
	final private static String badDir = rootDir + "errors/";


	@Test
	public void empty() throws IOException {
		parse(load(badDir + "empty.boa"),
			new String[] { "1,0: no viable alternative at input '<EOF>'" });
	}

	@Test
	public void keywordAsId() throws IOException {
		parse(load(badDir + "keyword-as-id.boa"),
			new String[] { "2,7: keyword 'output' can not be used as an identifier" });
	}
}
