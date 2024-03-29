/*
 * Copyright 2016, Hridesh Rajan, Robert Dyer, Neha Bhide
 *                 Iowa State University of Science and Technology
 *                 and Bowling Green State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.test.compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.BeforeClass;

import boa.compiler.SymbolTable;
import boa.compiler.ast.Start;
import boa.compiler.transforms.InheritedAttributeTransformer;
import boa.compiler.transforms.LocalAggregationTransformer;
import boa.compiler.transforms.RecursiveFunctionTransformer;
import boa.compiler.transforms.VariableDeclRenameTransformer;
import boa.compiler.transforms.VisitorOptimizingTransformer;
import boa.compiler.visitors.CodeGeneratingVisitor;
import boa.compiler.visitors.PrettyPrintVisitor;
import boa.compiler.visitors.TypeCheckingVisitor;
import boa.parser.BoaLexer;
import boa.parser.BoaParser;
import boa.parser.BoaParser.StartContext;

/**
 * @author rdyer
 * @author nbhide
 */
public abstract class BaseTest {
	protected static boolean DEBUG = false;

	@BeforeClass
	public static void initializeSymbols() throws IOException {
		SymbolTable.initialize(new ArrayList<URL>());
	}

	@Before
	public void resetCustomTypes() throws IOException {
		SymbolTable.resetTypeMap();
	}

	@Before
	public void disableDebug() throws IOException {
		DEBUG = false;
	}


	//
	// lexing
	//

	protected CommonTokenStream lex(final String input) throws IOException {
		return lex(input, new int[0], new String[0]);
	}

	protected CommonTokenStream lex(final String input, final int[] ids, final String[] strings) throws IOException {
		return lex(input, ids, strings, new String[0]);
	}

	protected CommonTokenStream lex(final String input, final int[] ids, final String[] strings, final String[] errors) throws IOException {
		final List<String> foundErr = new ArrayList<String>();
		final BoaLexer lexer = new BoaLexer(new ANTLRInputStream(new StringReader(input)));
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {
			@Override
			public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
				foundErr.add(line + "," + charPositionInLine + ": " + msg);
			}
		});

		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		tokens.fill();

		if (ids.length > 0 && strings.length > 0)
			assertEquals("ids != strings", ids.length, strings.length);

		if (ids.length > 0) {
			final List<Token> t = tokens.getTokens();
			if (DEBUG) {
				for (int i = 0; i < t.size(); i++) {
					final Token token = t.get(i);
					System.out.print(token.getType() + ", ");
				}
				System.out.println();
				for (int i = 0; i < t.size(); i++) {
					final Token token = t.get(i);
					System.out.print(token.getText() + ", ");
				}
				System.out.println();
				System.out.println();
			}
			assertEquals("wrong number of tokens", ids.length, t.size());
			for (int i = 0; i < t.size(); i++)
				assertEquals("wrong token type", ids[i], t.get(i).getType());
		}

		if (strings.length > 0) {
			final List<Token> t = tokens.getTokens();
			assertEquals("wrong number of tokens", strings.length, t.size());
			for (int i = 0; i < t.size(); i++)
				assertEquals("wrong token type", strings[i], t.get(i).getText());
		}

		assertEquals("wrong number of errors: " + input, errors.length, foundErr.size());
		for (int i = 0; i < foundErr.size(); i++)
			assertEquals("wrong error", errors[i], foundErr.get(i));

		return tokens;
	}


	//
	// parsing
	//

	protected StartContext parse(final String input) throws IOException {
		return parse(input, new String[0]);
	}

	protected StartContext parse(final String input, final String[] errors) throws IOException {
		final CommonTokenStream tokens = lex(input);
		final BoaParser parser = new BoaParser(tokens);

		final List<String> foundErr = new ArrayList<String>();
		parser.removeErrorListeners();
		parser.addErrorListener(new BaseErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) throws ParseCancellationException {
				throw new ParseCancellationException(e);
			}
		});

		parser.setBuildParseTree(false);
		parser.getInterpreter().setPredictionMode(PredictionMode.SLL);

		StartContext p;
		try {
			p = parser.start();
		} catch (final Exception e) {
			// fall-back to LL mode parsing if SLL fails
			tokens.reset();
			parser.reset();

			parser.removeErrorListeners();
			parser.addErrorListener(new BaseErrorListener() {
				@Override
				public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
					foundErr.add(line + "," + charPositionInLine + ": " + msg);
				}
			});
			parser.getInterpreter().setPredictionMode(PredictionMode.LL);

			p = parser.start();
		}

		if (!DEBUG)
			assertEquals("wrong number of errors", errors.length, foundErr.size());
		for (int i = 0; i < foundErr.size(); i++) {
			if (DEBUG)
				System.out.println(foundErr.get(i));
			else
				assertEquals("wrong error", errors[i], foundErr.get(i));
		}

		return p;
	}


	//
	// type checking
	//

	protected StartContext typecheck(final String input) throws IOException {
		return typecheck(input, null);
	}

	protected StartContext typecheck(final String input, final String error) throws IOException {
		final StartContext ctx = parse(input);

		try {
			TypeCheckingVisitor.warn = false;
			TypeCheckingVisitor.instance.start(ctx.ast, new SymbolTable());
			if (error != null)
				fail("expected error: " + error);
		} catch (final Exception e) {
			if (error == null)
				fail("found unexpected error: " + e.getMessage());
			else
				assertEquals(error, e.getMessage());
		}

		return ctx;
	}

	
	//
	// code generation
	//

	protected StartContext codegen(final String input) throws IOException {
		return codegen(input, null);
	}

	protected StartContext codegen(final String input, final String error) throws IOException {
		final File outputRoot = new File(new File(System.getProperty("java.io.tmpdir")), UUID.randomUUID().toString());
		final File outputSrcDir = new File(outputRoot, "boa");
		if (!outputSrcDir.mkdirs())
			throw new IOException("unable to mkdir " + outputSrcDir);
		final File outputFile = new File(outputSrcDir, "Test.java");

		final StartContext ctx = typecheck(input);
		final Start p = ctx.ast;
		// use the whole input string to seed the RNG
		final int seed = new PrettyPrintVisitor().startAndReturn(p).hashCode();

		try {
			new VariableDeclRenameTransformer().start(p);
			new InheritedAttributeTransformer().start(p);
			new LocalAggregationTransformer().start(p);
			new RecursiveFunctionTransformer().start(p);
			new VisitorOptimizingTransformer().start(p);

			final CodeGeneratingVisitor cg = new CodeGeneratingVisitor("Test", true, seed, false);
			cg.start(p);

			try (final BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(outputFile))) {
				o.write(cg.getCode().getBytes());
			}

			final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
			final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
			final Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(new File[] { outputFile }));

			if (!compiler.getTask(null, fileManager, diagnostics, Arrays.asList(new String[] { "-cp", System.getProperty("java.class.path") }), null, compilationUnits).call())
				for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics())
					throw new RuntimeException("Error on line " + diagnostic.getLineNumber() + ": " + diagnostic.getMessage(null));

			if (error != null)
				fail("expected to see exception: " + error);
		} catch (final Exception e) {
			if (error == null) {
				if (e.getMessage() == null) {
					e.printStackTrace();
					fail("unexpected exception");
				} else
					fail("found unexpected exception: " + e.getMessage());
			} else
				assertThat(e.getMessage(), RegexMatcher.matches(error));
		}

		delete(outputSrcDir);

		return ctx;
	}


	//
	// misc utils
	//

	protected String load(final String fileName) throws IOException {
		final StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		FileReader fr = null;
		try {
			fr = new FileReader(fileName);
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
		return sb.toString();
	}

	protected final void delete(final File f) throws IOException {
		if (f.isDirectory())
			for (final File g : f.listFiles())
				delete(g);

		if (!f.delete())
			throw new IOException("unable to delete file " + f);
	}

	public static class RegexMatcher extends BaseMatcher {
		private final String regex;

		public RegexMatcher(final String regex) {
			this.regex = regex;
		}

		public boolean matches(final Object o) {
			return ((String)o).matches(regex);
		}

		public void describeTo(final Description description) {
			description.appendText("matches regex=" + regex);
		}

		public static RegexMatcher matches(final String regex) {
			return new RegexMatcher(regex);
		}
	}
}
