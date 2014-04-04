package boa.test.compiler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import org.antlr.v4.runtime.atn.PredictionMode;

import boa.parser.BoaLexer;
import boa.parser.BoaParser;

@RunWith(JUnit4.class)
public class TestParser {
	final private static String rootDir = "test/parsing/";
	final private static String badDir = rootDir + "errors/";


	@Test
	public void empty() throws IOException {
		expectErrors(load(badDir + "empty.boa"),
			new String[] { "1,0: no viable alternative at input '<EOF>'" });
	}

	@Test
	public void keywordAsId() throws IOException {
		expectErrors(load(badDir + "keyword-as-id.boa"),
			new String[] { "2,7: keyword 'output' can not be used as an identifier" });
	}


	///////////////////////////////////////////

	private void expectErrors(final String input, final String[] strings) throws IOException {
		expectTree(input, strings);
	}

	private void expectNoErrors(final String input) throws IOException {
		expectTree(input, new String[0]);
	}

	private void expectTree(final String input) throws IOException {
		expectTree(input, new String[0]);
	}

	private void expectTree(final String input, final String[] errors) throws IOException {
		final List<String> foundErr = new ArrayList<String>();
		final BoaParser parser = new BoaParser(new CommonTokenStream(new BoaLexer(new ANTLRInputStream(new StringReader(input)))));
		parser.removeErrorListeners();
		parser.addErrorListener(new BaseErrorListener () {
			@Override
			public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
				foundErr.add(line + "," + charPositionInLine + ": " + msg);
			}
		});

		parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
		parser.start();

		if (errors == null) {
			assertTrue("Expected errors", foundErr.size() > 0);
		} else {
			assertEquals("wrong number of errors", errors.length, foundErr.size());
			for (int i = 0; i < foundErr.size(); i++)
				assertEquals("wrong error", errors[i], foundErr.get(i));
		}
	}

	private String load(final String fileName) throws IOException {
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(fileName));
			final byte[] bytes = new byte[(int) new File(fileName).length()];
			in.read(bytes);
			return new String(bytes);
		} finally {
			if (in != null)
				in.close();
		}
	}
}
