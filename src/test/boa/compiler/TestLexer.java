import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RecognitionException;

import boa.parser.BoaLexer;

@RunWith(JUnit4.class)
public class TestLexer {
	@Test
	public void commentEOFnoEOL() throws IOException {
		lexFile("comment-eof-no-eol");
	}

	@Test
	public void badComment() throws IOException {
		lexErrorFile("bad-comment", "TODO");
	}

	public void lexFile(final String path) throws IOException {
		final BoaLexer lexer = new BoaLexer(new ANTLRFileStream("test/lexing/" + path + ".boa"));
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {
			@Override
			public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
				Assert.assertEquals("Unexpected lexing error: " + msg, false, true);
			}
		});
		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		tokens.fill();
	}

	public void lexErrorFile(final String path, final String err) throws IOException {
		final BoaLexer lexer = new BoaLexer(new ANTLRFileStream("test/lexing/errors/" + path + ".boa"));
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {
			@Override
			public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
				Assert.assertEquals("Lexer error did not match", msg, err);
			}
		});
		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		tokens.fill();
	}
}
