package boa.test.compiler;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;

import boa.parser.BoaLexer;

@RunWith(JUnit4.class)
public class TestLexer extends BaseTest {
	final private static String rootDir = "test/lexing/";
	final private static String badDir = rootDir + "errors/";


	@Test
	public void empty() throws IOException {
		expectTokens(load(rootDir + "empty.boa"),
			new int[] { BoaLexer.EOF },
			new String[] { "<EOF>" });
	}

	@Test
	public void commentEOFnoEOL() throws IOException {
		expectTokens(load(rootDir + "comment-eof-no-eol.boa"),
			new int[] { BoaLexer.EOF },
			new String[] { "<EOF>" });
	}

	@Test
	public void badComment() throws IOException {
		expectTokens(load(badDir + "bad-comment.boa"),
			new int[] { BoaLexer.Identifier, BoaLexer.Identifier, BoaLexer.EOF },
			new String[] { "bad", "comment", "<EOF>" },
			new String[] { "1,0: token recognition error at: '@'" });
	}


	///////////////////////////////////////////

	private void expectTokens(final String input, final int[] ids, final String[] strings) throws IOException {
		expectTokens(input, ids, strings, new String[0]);
	}

	private void expectTokens(final String input, final int[] ids, final String[] strings, final String[] errors) throws IOException {
		final List<String> foundErr = new ArrayList<String>();
		final BoaLexer lexer = new BoaLexer(new ANTLRInputStream(new StringReader(input)));
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener () {
			@Override
			public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
				foundErr.add(line + "," + charPositionInLine + ": " + msg);
			}
		});

		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		tokens.fill();

		assertEquals("ids != strings", ids.length, strings.length);
		if (ids.length > 0) {
			final List<Token> t = tokens.getTokens();
			assertEquals("wrong number of tokens", ids.length, t.size());

			for (int i = 0; i < t.size(); i++) {
				final Token token = t.get(i);
				assertEquals("wrong token type", ids[i], token.getType());
				assertEquals("wrong token type", strings[i], token.getText());
			}
		}

		if (errors == null) {
			assertTrue("Expected errors", foundErr.size() > 0);
		} else {
			assertEquals("wrong number of errors: " + input, errors.length, foundErr.size());
			for (int i = 0; i < foundErr.size(); i++)
				assertEquals("wrong error", errors[i], foundErr.get(i));
		}
	}
}
