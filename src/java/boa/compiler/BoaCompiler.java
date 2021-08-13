/*
 * Copyright 2017, Anthony Urso, Hridesh Rajan, Robert Dyer, Neha Bhide
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
package boa.compiler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.scannotation.ClasspathUrlFinder;

import boa.BoaMain;
import boa.compiler.ast.Start;
import boa.compiler.listeners.BoaErrorListener;
import boa.compiler.listeners.LexerErrorListener;
import boa.compiler.listeners.ParserErrorListener;
import boa.compiler.transforms.InheritedAttributeTransformer;
import boa.compiler.transforms.LocalAggregationTransformer;
import boa.compiler.transforms.RecursiveFunctionTransformer;
import boa.compiler.transforms.VariableDeclRenameTransformer;
import boa.compiler.transforms.VisitorOptimizingTransformer;
import boa.compiler.visitors.ASTPrintingVisitor;
import boa.compiler.visitors.CodeGeneratingVisitor;
import boa.compiler.visitors.PrettyPrintVisitor;
import boa.compiler.visitors.TaskClassifyingVisitor;
import boa.compiler.visitors.TypeCheckingVisitor;
import boa.datagen.DefaultProperties;
import boa.parser.BoaLexer;
import boa.parser.BoaParser;

/**
 * The main entry point for the Boa compiler.
 *
 * @author anthonyu
 * @author rdyer
 * @author nbhide
 */
public class BoaCompiler extends BoaMain {
	private static Logger LOG = Logger.getLogger(BoaCompiler.class);

	public static void main(final String[] args) throws IOException {
		final CommandLine cl = processCommandLineOptions(args);
		if (cl == null) return;
		final File inputFile = BoaCompiler.inputFile;

		// get the name of the generated class
		final String className = getGeneratedClass(cl);

		// get the filename of the jar we will be writing
		final String jarName;
		if (cl.hasOption('o'))
			jarName = cl.getOptionValue('o');
		else
			jarName = className + ".jar";

		// make the output directory
		File outputRoot = null;
		if (cl.hasOption("cd")) {
			outputRoot = new File(cl.getOptionValue("cd"));
		} else {
			outputRoot = new File(new File(System.getProperty("java.io.tmpdir")), UUID.randomUUID().toString());
		}
		final File outputSrcDir = new File(outputRoot, "boa");
		if (!outputSrcDir.mkdirs())
			throw new IOException("unable to mkdir " + outputSrcDir);

		// find custom libs to load
		final List<URL> libs = new ArrayList<URL>();
		if (cl.hasOption('l'))
			for (final String lib : cl.getOptionValues('l'))
				libs.add(new File(lib).toURI().toURL());

		SymbolTable.initialize(libs);

		try {
			final BoaLexer lexer = new BoaLexer(new ANTLRFileStream(inputFile.getAbsolutePath()));
			lexer.removeErrorListeners();
			lexer.addErrorListener(new LexerErrorListener());

			final CommonTokenStream tokens = new CommonTokenStream(lexer);
			final BoaParser parser = new BoaParser(tokens);
			parser.removeErrorListeners();
			parser.addErrorListener(new BaseErrorListener() {
				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) throws ParseCancellationException {
					throw new ParseCancellationException(e);
				}
			});

			final BoaErrorListener parserErrorListener = new ParserErrorListener();
			final Start p = parse(tokens, parser, parserErrorListener);
			if (cl.hasOption("ast")) new ASTPrintingVisitor().start(p);
			// use the whole input string to seed the RNG
			final int seed = new PrettyPrintVisitor().startAndReturn(p).hashCode();

			try {
				if (!parserErrorListener.hasError) {
					TypeCheckingVisitor.instance.start(p, new SymbolTable());

					final TaskClassifyingVisitor simpleVisitor = new TaskClassifyingVisitor();
					simpleVisitor.start(p);
					final boolean isSimple = !simpleVisitor.isComplex();
					LOG.info(inputFile.getName() + ": task complexity: " + (isSimple ? "simple" : "complex"));

					new VariableDeclRenameTransformer().start(p);
					if (cl.hasOption("ppall")) {
						System.out.println("==> AFTER VariableDeclRenameTransformer");
						new PrettyPrintVisitor().start(p);
					}
					new InheritedAttributeTransformer().start(p);
					if (cl.hasOption("ppall")) {
						System.out.println("==> AFTER InheritedAttributeTransformer");
						new PrettyPrintVisitor().start(p);
					}
					new LocalAggregationTransformer().start(p);
					if (cl.hasOption("ppall")) {
						System.out.println("==> AFTER LocalAggregationTransformer");
						new PrettyPrintVisitor().start(p);
					}
					new RecursiveFunctionTransformer().start(p);
					if (cl.hasOption("ppall")) {
						System.out.println("==> AFTER RecursiveFunctionTransformer");
						new PrettyPrintVisitor().start(p);
					}
					new VisitorOptimizingTransformer().start(p);
					if (cl.hasOption("ppall")) {
						System.out.println("==> AFTER VisitorOptimizingTransformer");
						new PrettyPrintVisitor().start(p);
					}

					if (cl.hasOption("pp")) new PrettyPrintVisitor().start(p);
					if (cl.hasOption("ast2")) new ASTPrintingVisitor().start(p);

					final CodeGeneratingVisitor cg = new CodeGeneratingVisitor(className, isSimple ? 64 * 1024 * 1024 : 10 * 1024 * 1024, seed, DefaultProperties.localDataPath != null);
					cg.start(p);

					final File outputFile = new File(outputSrcDir, className + ".java");
					try (final BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(outputFile))) {
						o.write(cg.getCode().getBytes());
					}

					compileGeneratedSrc(cl, jarName, outputRoot, outputFile);
				} else {
					System.exit(-1);
				}
			} catch (final TypeCheckException e) {
				parserErrorListener.error("typecheck", lexer, null, e.n.beginLine, e.n.beginColumn, e.n2.endColumn - e.n.beginColumn + 1, e.getMessage(), e);
				System.exit(-1);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			throw new RuntimeException(inputFile.getName() + ": compilation failed", e);
		}
	}

	public static void parseOnly(final String[] args) throws IOException {
		final CommandLine cl = processParseCommandLineOptions(args);
		if (cl == null) return;

		// find custom libs to load
		final List<URL> libs = new ArrayList<URL>();
		if (cl.hasOption('l'))
			for (final String lib : cl.getOptionValues('l'))
				libs.add(new File(lib).toURI().toURL());

		SymbolTable.initialize(libs);

		try {
			final BoaLexer lexer = new BoaLexer(new ANTLRFileStream(inputFile.getAbsolutePath()));
			lexer.removeErrorListeners();
			lexer.addErrorListener(new LexerErrorListener());

			final CommonTokenStream tokens = new CommonTokenStream(lexer);
			final BoaParser parser = new BoaParser(tokens);
			parser.removeErrorListeners();
			parser.addErrorListener(new BaseErrorListener() {
				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) throws ParseCancellationException {
					throw new ParseCancellationException(e);
				}
			});

			final BoaErrorListener parserErrorListener = new ParserErrorListener();
			final Start p = parse(tokens, parser, parserErrorListener);

			try {
				if (!parserErrorListener.hasError) {
					TypeCheckingVisitor.instance.start(p, new SymbolTable());

					final TaskClassifyingVisitor simpleVisitor = new TaskClassifyingVisitor();
					simpleVisitor.start(p);

					LOG.info(inputFile.getName() + ": task complexity: " + (!simpleVisitor.isComplex() ? "simple" : "complex"));
				}
			} catch (final TypeCheckException e) {
				parserErrorListener.error("typecheck", lexer, null, e.n.beginLine, e.n.beginColumn, e.n2.endColumn - e.n.beginColumn + 1, e.getMessage(), e);
			}
		} catch (final Exception e) {
			System.err.print(inputFile.getName() + ": parsing failed: ");
			e.printStackTrace();
		}
	}

	private static Start parse(final CommonTokenStream tokens, final BoaParser parser, final BoaErrorListener parserErrorListener) {
		parser.setBuildParseTree(false);
		parser.getInterpreter().setPredictionMode(PredictionMode.SLL);

		try {
			return parser.start().ast;
		} catch (final ParseCancellationException e) {
			// fall-back to LL mode parsing if SLL fails
			tokens.reset();
			parser.reset();

			parser.removeErrorListeners();
			parser.addErrorListener(parserErrorListener);
			parser.getInterpreter().setPredictionMode(PredictionMode.LL);

			return parser.start().ast;
		}
	}

	private static void compileGeneratedSrc(final CommandLine cl, final String jarName, final File outputRoot, final File outputFile)
			throws RuntimeException, IOException, FileNotFoundException {
		// compile the generated .java file
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null)
			throw new RuntimeException("Could not get javac - are you running the Boa compiler with a JDK or a JRE?");
		LOG.info("compiling: " + outputFile);
		LOG.info("classpath: " + System.getProperty("java.class.path"));
		if (compiler.run(null, null, null, "-source", "8", "-target", "8", "-cp", System.getProperty("java.class.path"), outputFile.toString()) != 0)
			throw new RuntimeException("compile failed");

		final List<File> libJars = new ArrayList<File>();

		if (cl.hasOption('j')) {
			libJars.add(new File(cl.getOptionValue('j')));
		} else {
			// find the location of the jar this class is in
			final String path = ClasspathUrlFinder.findClassBase(BoaCompiler.class).getPath();
			// find the location of the compiler distribution
			final File root = new File(path.substring(path.indexOf(':') + 1, path.indexOf('!'))).getParentFile();

			libJars.add(new File(root, "boa-runtime.jar"));
		}

		if (cl.hasOption('l'))
			for (final String s : Arrays.asList(cl.getOptionValues('l')))
				libJars.add(new File(s));

		generateJar(jarName, outputRoot, libJars);

		if (DefaultProperties.localDataPath == null) {
			delete(outputRoot);
		}
	}

	static File inputFile = null;

	private static CommandLine processCommandLineOptions(final String[] args) {
		// parse the command line options
		final Options options = new Options();
		options.addOption("l", "libs", true, "extra jars (functions/aggregators) to be compiled in");
		options.addOption("i", "in", true, "file to be compiled");
		options.addOption("o", "out", true, "the name of the resulting jar");
		options.addOption("j", "rtjar", true, "the path to the Boa runtime jar");
		options.addOption("n", "name", true, "the name of the generated main class");
		options.addOption("ast", "ast-parsed", false, "print the AST immediately after parsing (debug)");
		options.addOption("ast2", "ast-transformed", false, "print the AST after transformations, before code generation (debug)");
		options.addOption("pp", "pretty-print", false, "pretty print the AST before code generation (debug)");
		options.addOption("ppall", "pretty-print-all", false, "pretty print the AST after each transform (debug)");
		options.addOption("cd", "compilation-dir", true, "directory to store all generated files");

		final CommandLine cl;
		try {
			cl = new PosixParser().parse(options, args);
		} catch (final org.apache.commons.cli.ParseException e) {
			System.err.println(e.getMessage());
			new HelpFormatter().printHelp("Boa Compiler", options);
			return null;
		}

		// get the filename of the program we will be compiling
		inputFile = null;
		if (cl.hasOption('i')) {
			final String inputPath = cl.getOptionValue('i');

			final File f = new File(inputPath);
			if (!f.exists())
				System.err.println("File '" + inputPath + "' does not exist, skipping");
			else
				inputFile = f;
		}

		if (inputFile == null) {
			System.err.println("no valid input file found - did you use the --in option?");
			new HelpFormatter().printHelp("Boa Compiler", options);
			return null;
		}

		return cl;
	}

	private static CommandLine processParseCommandLineOptions(final String[] args) {
		// parse the command line options
		final Options options = new Options();
		options.addOption("l", "libs", true, "extra jars (functions/aggregators) to be compiled in");
		options.addOption("i", "in", true, "file to be parsed");

		final CommandLine cl;
		try {
			cl = new PosixParser().parse(options, args);
		} catch (final org.apache.commons.cli.ParseException e) {
			printHelp(options, e.getMessage());
			return null;
		}

		// get the filename of the program we will be compiling
		inputFile = null;
		if (cl.hasOption('i')) {
			final String inputPath = cl.getOptionValue('i');

			final File f = new File(inputPath);
			if (!f.exists())
				System.err.println("File '" + inputPath + "' does not exist, skipping");
			else
				inputFile = f;
		}

		if (inputFile == null) {
			printHelp(options, "no valid input file found - did you use the --in option?");
			return null;
		}

		return cl;
	}

	// get the name of the generated class
	private static final String getGeneratedClass(final CommandLine cl) {
		String className;
		if (cl.hasOption('n')) {
			className = cl.getOptionValue('n');
		} else {
			className = jarToClassname(inputFile);
		}
		return className;
	}

	private static final void delete(final File f) throws IOException {
		if (f.isDirectory())
			for (final File g : f.listFiles())
				delete(g);

		if (!f.delete())
			throw new IOException("unable to delete file " + f);
	}

	private static void generateJar(final String jarName, final File dir, final List<File> libJars) throws IOException, FileNotFoundException {
		final JarOutputStream jar = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(new File(jarName))));

		try {
			final int offset = dir.toString().length() + 1;

			for (final File f : findFiles(dir, new ArrayList<File>()))
				putJarEntry(jar, f, f.getPath().substring(offset));

			for (final File f : libJars)
				putJarEntry(jar, f, "lib" + File.separatorChar + f.getName());
		} finally {
			jar.close();
		}
	}

	private static final List<File> findFiles(final File f, final List<File> l) {
		if (f.isDirectory())
			for (final File g : f.listFiles())
				findFiles(g, l);
		else
			l.add(f);

		return l;
	}

	private static void putJarEntry(final JarOutputStream jar, final File f, final String path) throws IOException {
		jar.putNextEntry(new ZipEntry(path));

		final InputStream in = new BufferedInputStream(new FileInputStream(f));
		try {
			final byte[] b = new byte[4096];
			int len;
			while ((len = in.read(b)) > 0)
				jar.write(b, 0, len);
		} finally {
			in.close();
		}

		jar.closeEntry();
	}
}
