package boa.test.compiler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;

import boa.parser.BoaLexer;
import boa.parser.BoaParser;

@RunWith(Parameterized.class)
public class TestGood {
	final private static String goodDir = "test/known-good/";

	@Parameters(name = "{0}")
	public static List<String[]> data() {
		final List<String[]> files = new ArrayList<String[]>();
		for (final File f : new File(goodDir).listFiles())
			if (!f.isDirectory())
				files.add(new String[] { f.getPath(), null });
		return files;
	}

	private String fileName;
	public TestGood(final String fileName, final String ignored) {
		this.fileName = fileName;
	}

	// test a bunch of known good files
	@Test
	public void knownGood() throws IOException {
		expectTree(load(fileName), new String[0]);
	}


	///////////////////////////////////////////

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
