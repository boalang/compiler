package boa.test.compiler;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import boa.parser.BoaLexer;

@RunWith(JUnit4.class)
public class TestLexer extends BaseTest {
	final private static String rootDir = "test/lexing/";
	final private static String badDir = rootDir + "errors/";


	@Test
	public void empty() throws IOException {
		lex(load(rootDir + "empty.boa"),
			new int[] { BoaLexer.EOF },
			new String[] { "<EOF>" });
	}

	@Test
	public void commentEOFnoEOL() throws IOException {
		lex(load(rootDir + "comment-eof-no-eol.boa"),
			new int[] { BoaLexer.EOF },
			new String[] { "<EOF>" });
	}

	@Test
	public void badComment() throws IOException {
		lex(load(badDir + "bad-comment.boa"),
			new int[] { BoaLexer.Identifier, BoaLexer.Identifier, BoaLexer.EOF },
			new String[] { "bad", "comment", "<EOF>" },
			new String[] { "1,0: token recognition error at: '@'" });
	}
}
