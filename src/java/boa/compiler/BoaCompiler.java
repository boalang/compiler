/*
 * Copyright 2017, Anthony Urso, Hridesh Rajan, Robert Dyer, Neha Bhide, Che Shian Hung,
 *                 Iowa State University of Science and Technology,
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
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.stringtemplate.v4.ST;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

import org.scannotation.ClasspathUrlFinder;

import boa.BoaMain;
import boa.compiler.ast.Program;
import boa.compiler.ast.Start;
import boa.compiler.transforms.InheritedAttributeTransformer;
import boa.compiler.transforms.LocalAggregationTransformer;
import boa.compiler.transforms.VariableDeclRenameTransformer;
import boa.compiler.transforms.VisitorMergingTransformer;
import boa.compiler.transforms.VisitorOptimizingTransformer;
import boa.compiler.transforms.ViewTransformer;
import boa.compiler.visitors.AbstractCodeGeneratingVisitor;
import boa.compiler.visitors.ASTPrintingVisitor;
import boa.compiler.visitors.CodeGeneratingVisitor;
import boa.compiler.visitors.PrettyPrintVisitor;
import boa.compiler.visitors.TaskClassifyingVisitor;
import boa.compiler.visitors.TypeCheckingVisitor;
import boa.compiler.visitors.ViewFindingVisitor;
import boa.compiler.listeners.BoaErrorListener;
import boa.compiler.listeners.LexerErrorListener;
import boa.compiler.listeners.ParserErrorListener;
import boa.compiler.WorkflowGenerator;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RecognitionException;

import boa.datagen.DefaultProperties;
import boa.parser.BoaParser;
import boa.parser.BoaLexer;

/**
 * The main entry point for the Boa compiler.
 *
 * @author anthonyu
 * @author rdyer
 * @author nbhide
 */
public class BoaCompiler extends BoaMain {
	private static int jobId = 0;

	private static Logger LOG = Logger.getLogger(BoaCompiler.class);

	private static Map<String, String> viewIds = null;
	private static Map<String, String> viewSrcPaths = null;
	private static Map<String, Start> viewASTs = null;

	public static void main(final String[] args) throws IOException {
		CommandLine cl = processCommandLineOptions(args);
		if (cl == null) return;
		final ArrayList<File> inputFiles = BoaCompiler.inputFiles;

		if (cl.hasOption("job"))
			jobId = Integer.parseInt(cl.getOptionValue("job"));
		else
			jobId = 0;

		// get the name of the generated class
		final String className = getGeneratedClass(cl);

		// get the filename of the jar we will be writing
		final String jarName;
		if (cl.hasOption('o'))
			jarName = cl.getOptionValue('o');
		else
			jarName = className + ".jar";

		Map<String, Program> subViews = null;
		viewIds = generateViewIds(cl);
		viewASTs = generateViewASTs(cl);
		viewSrcPaths = generateViewSrcPaths(cl);
		ViewFindingVisitor vfv = new ViewFindingVisitor();

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

		final File outputFile = new File(outputSrcDir, className + ".java");
		final BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(outputFile));
		try {
			final List<String> jobnames = new ArrayList<String>();
			final List<String> jobs = new ArrayList<String>();
			final List<Integer> seeds = new ArrayList<Integer>();
			boolean isSimple = true;

			final List<Program> visitorPrograms = new ArrayList<Program>();

			SymbolTable.initialize(libs);

			final int maxVisitors;
			if (cl.hasOption('v'))
				maxVisitors = Integer.parseInt(cl.getOptionValue('v'));
			else
				maxVisitors = Integer.MAX_VALUE;

			for (int i = 0; i < inputFiles.size(); i++) {
				final File f = inputFiles.get(i);
				try {
					final BoaLexer lexer = new BoaLexer(new ANTLRFileStream(f.getAbsolutePath()));
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

					if (cl.hasOption("views")) {
						vfv.start(p);
						System.exit(0);
					}
					if (cl.hasOption("ast")) new ASTPrintingVisitor().start(p);
					// use the whole input string to seed the RNG
					seeds.add(new PrettyPrintVisitor().startAndReturn(p).hashCode());

					final String jobName = Integer.toString(jobId);
					// jobId++;

					try {
						if (!parserErrorListener.hasError) {
							TypeCheckingVisitor tcv = new TypeCheckingVisitor();
							tcv.setViewASTs(viewASTs);
							tcv.start(p, new SymbolTable());
							vfv.start(p);

							final TaskClassifyingVisitor simpleVisitor = new TaskClassifyingVisitor();
							simpleVisitor.start(p);

							LOG.info(f.getName() + ": task complexity: " + (!simpleVisitor.isComplex() ? "simple" : "complex"));
							isSimple &= !simpleVisitor.isComplex();

							new VariableDeclRenameTransformer().start(p);
							new InheritedAttributeTransformer().start(p);
							new LocalAggregationTransformer().start(p);
							new ViewTransformer().start(p);

							// if a job has no visitor, let it have its own method
							// also let jobs have own methods if visitor merging is disabled
							if (!simpleVisitor.isComplex() || maxVisitors < 2 || inputFiles.size() == 1) {
								new VisitorOptimizingTransformer().start(p);

								if (cl.hasOption("pp")) new PrettyPrintVisitor().start(p);
								if (cl.hasOption("ast2")) new ASTPrintingVisitor().start(p);
								final CodeGeneratingVisitor cg = new CodeGeneratingVisitor(jobName);
								cg.start(p);
								subViews = cg.getSubViewsMap();
								jobs.add(cg.getCode());

								jobnames.add(jobName);
							}
							// if a job has visitors, fuse them all together into a single program
							else {
								p.getProgram().jobName = jobName;
								visitorPrograms.add(p.getProgram());
							}
						}
					} catch (final TypeCheckException e) {
						parserErrorListener.error("typecheck", lexer, null, e.n.beginLine, e.n.beginColumn, e.n2.endColumn - e.n.beginColumn + 1, e.getMessage(), e);
					}
				} catch (final Exception e) {
					System.err.print(f.getName() + ": compilation failed: ");
					e.printStackTrace();
				}
			}

			if (!visitorPrograms.isEmpty())
				try {
					for (final Program p : new VisitorMergingTransformer().mergePrograms(visitorPrograms, maxVisitors)) {
						new VisitorOptimizingTransformer().start(p);

						if (cl.hasOption("pp")) new PrettyPrintVisitor().start(p);
						if (cl.hasOption("ast2")) new ASTPrintingVisitor().start(p);
						final CodeGeneratingVisitor cg = new CodeGeneratingVisitor(p.jobName);
						cg.start(p);
						subViews = cg.getSubViewsMap();
						jobs.add(cg.getCode());

						jobnames.add(p.jobName);
					}
				} catch (final Exception e) {
					System.err.println("error fusing visitors - falling back: " + e);
					e.printStackTrace();

					for (final Program p : visitorPrograms) {
						new VisitorOptimizingTransformer().start(p);

						if (cl.hasOption("pp")) new PrettyPrintVisitor().start(p);
						if (cl.hasOption("ast2")) new ASTPrintingVisitor().start(p);
						final CodeGeneratingVisitor cg = new CodeGeneratingVisitor(p.jobName);
						cg.start(p);
						subViews = cg.getSubViewsMap();
						jobs.add(cg.getCode());

						jobnames.add(p.jobName);
					}
				}

			if (jobs.size() == 0)
				throw new RuntimeException("no files compiled without error");

			final ST st = AbstractCodeGeneratingVisitor.stg.getInstanceOf("Program");

			st.add("name", className);
			st.add("numreducers", inputFiles.size());
			st.add("jobs", jobs);
			st.add("jobId", jobId);
			st.add("jobnames", jobnames);
			st.add("combineTables", CodeGeneratingVisitor.combineAggregatorStrings);
			st.add("reduceTables", CodeGeneratingVisitor.reduceAggregatorStrings);
			st.add("splitsize", isSimple ? 64 * 1024 * 1024 : 10 * 1024 * 1024);
			st.add("seeds", seeds);
			if (DefaultProperties.localDataPath != null) {
				st.add("isLocal", true);
			}

			o.write(st.render().getBytes());
		} finally {
			o.close();
		}

		String jarDirName = "jar";
		if (cl.hasOption("jardir"))
			jarDirName = cl.getOptionValue("jardir");

		String wfDirName = "workflow";
		if (cl.hasOption("wfdir"))
			wfDirName = cl.getOptionValue("wfdir");

		final File jarDir = new File(jarDirName);
		if (jarDir.isDirectory())
			delete(jarDir);
		if (!jarDir.mkdirs())
			throw new IOException("unable to mkdir " + jarDir);

		final File wfDir = new File(wfDirName);
		if (wfDir.isDirectory())
			delete(wfDir);
		if (!wfDir.mkdirs())
			throw new IOException("unable to mkdir " + wfDir);

		if (subViews.size() > 0) {
			for (final Map.Entry<String, Program> entry: subViews.entrySet()) {
				codegen(entry.getKey(), entry.getValue(), outputSrcDir, jarDir, Integer.toString(jobId), wfDir, cl);
			}
		}

		generateWorkflow(Integer.toString(jobId), vfv, new ArrayList<String>(), wfDir);
		compileGeneratedSrc(cl, jarName, jarDir, outputRoot, outputFile);
	}

	private static void codegen(String name, Program p, File srcDir, File jarDir, String wfName, File wfDir, CommandLine cl) throws IOException{
		if (cl == null) return;

		final String jarName = name + ".jar";

		Map<String, Program> subViews = null;

		final File outputSrcDir = new File(srcDir, name);
		jarDir = new File(jarDir, name);
		wfDir = new File(wfDir, name);
		wfName += "-" + name;

		if (!outputSrcDir.mkdirs())
			throw new IOException("unable to mkdir " + outputSrcDir);
		if (!jarDir.mkdirs())
			throw new IOException("unable to mkdir " + jarDir);
		if (!wfDir.mkdirs())
			throw new IOException("unable to mkdir " + wfDir);

		final List<URL> libs = new ArrayList<URL>();
		if (cl.hasOption('l'))
			for (final String lib : cl.getOptionValues('l'))
				libs.add(new File(lib).toURI().toURL());

		final File outputFile = new File(outputSrcDir, name + ".java");
		final BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(outputFile));
		try {
			final List<String> jobnames = new ArrayList<String>();
			final List<String> jobs = new ArrayList<String>();
			final List<Integer> seeds = new ArrayList<Integer>();
			boolean isSimple = true;

			final List<Program> visitorPrograms = new ArrayList<Program>();


			final int maxVisitors;
			if (cl.hasOption('v'))
				maxVisitors = Integer.parseInt(cl.getOptionValue('v'));
			else
				maxVisitors = Integer.MAX_VALUE;

			try {
				final String jobName = Integer.toString(jobId);
				// jobId++;
				seeds.add(new PrettyPrintVisitor().startAndReturn(p).hashCode());

				final TaskClassifyingVisitor simpleVisitor = new TaskClassifyingVisitor();
				simpleVisitor.start(p);

				LOG.info(name + ": task complexity: " + (!simpleVisitor.isComplex() ? "simple" : "complex"));
				isSimple &= !simpleVisitor.isComplex();

				new VariableDeclRenameTransformer().start(p);
				new InheritedAttributeTransformer().start(p);
				new LocalAggregationTransformer().start(p);
				new VisitorOptimizingTransformer().start(p);
				new ViewTransformer().start(p);

				if (cl.hasOption("pp")) new PrettyPrintVisitor().start(p);
				if (cl.hasOption("ast2")) new ASTPrintingVisitor().start(p);
				final CodeGeneratingVisitor cg = new CodeGeneratingVisitor(jobName);
				cg.start(p);
				subViews = cg.getSubViewsMap();

				jobs.add(cg.getCode());

				jobnames.add(jobName);

			} catch (final Exception e) {
				System.err.print(name + ": compilation failed: ");
				e.printStackTrace();
			}


			if (jobs.size() == 0)
				throw new RuntimeException("no files compiled without error");

			final ST st = AbstractCodeGeneratingVisitor.stg.getInstanceOf("Program");

			st.add("name", name);
			st.add("numreducers", 1);
			st.add("jobs", jobs);
			st.add("jobId", jobId);
			st.add("jobnames", jobnames);
			st.add("combineTables", CodeGeneratingVisitor.combineAggregatorStrings);
			st.add("reduceTables", CodeGeneratingVisitor.reduceAggregatorStrings);
			st.add("splitsize", isSimple ? 64 * 1024 * 1024 : 10 * 1024 * 1024);
			st.add("seeds", seeds);
			if (DefaultProperties.localDataPath != null) {
				st.add("isLocal", true);
			}

			o.write(st.render().getBytes());
		} finally {
			o.close();
		}


		if (subViews.size() > 0) {
			for (final Map.Entry<String, Program> entry: subViews.entrySet()) {
				codegen(entry.getKey(), entry.getValue(), outputSrcDir, jarDir, wfName, wfDir, cl);
			}
		}

		ViewFindingVisitor vfv = new ViewFindingVisitor();
		vfv.start(p);

		generateWorkflow(wfName, vfv, new ArrayList<String>(), wfDir);
		compileGeneratedSrc(cl, jarName, jarDir, outputSrcDir, outputFile);
	}

	public static void parseOnly(final String[] args) throws IOException {
		final CommandLine cl = processParseCommandLineOptions(args);
		if (cl == null) return;
		final ArrayList<File> inputFiles = BoaCompiler.inputFiles;

		// find custom libs to load
		final List<URL> libs = new ArrayList<URL>();
		if (cl.hasOption('l'))
			for (final String lib : cl.getOptionValues('l'))
				libs.add(new File(lib).toURI().toURL());

		SymbolTable.initialize(libs);

		for (int i = 0; i < inputFiles.size(); i++) {
			final File f = inputFiles.get(i);
			try {
				final BoaLexer lexer = new BoaLexer(new ANTLRFileStream(f.getAbsolutePath()));
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
						TypeCheckingVisitor tcv = new TypeCheckingVisitor();
						tcv.setViewASTs(viewASTs);
						tcv.start(p, new SymbolTable());

						final TaskClassifyingVisitor simpleVisitor = new TaskClassifyingVisitor();
						simpleVisitor.start(p);

						LOG.info(f.getName() + ": task complexity: " + (!simpleVisitor.isComplex() ? "simple" : "complex"));
					}
				} catch (final TypeCheckException e) {
					parserErrorListener.error("typecheck", lexer, null, e.n.beginLine, e.n.beginColumn, e.n2.endColumn - e.n.beginColumn + 1, e.getMessage(), e);
				}
			} catch (final Exception e) {
				System.err.print(f.getName() + ": parsing failed: ");
				e.printStackTrace();
			}
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

	private static void compileGeneratedSrc(final CommandLine cl, final String jarName, final File jarDir, final File outputRoot, final File outputFile)
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

		generateJar(jarName, outputRoot, jarDir, libJars);

		if (DefaultProperties.localDataPath == null) {
			delete(outputRoot);
		}
	}

	static ArrayList<File> inputFiles = null;

	private static CommandLine processCommandLineOptions(final String[] args) {
		// parse the command line options

		final Options options = new Options();
		options.addOption("l", "libs", true, "extra jars (functions/aggregators) to be compiled in");
		options.addOption("i", "in", true, "file(s) to be compiled (comma-separated list)");
		options.addOption("o", "out", true, "the name of the resulting jar");
		options.addOption("j", "rtjar", true, "the path to the Boa runtime jar");
		options.addOption("v", "visitors-fused", true, "number of visitors to fuse");
		options.addOption("n", "name", true, "the name of the generated main class");
		options.addOption("ast", "ast-parsed", false, "print the AST immediately after parsing (debug)");
		options.addOption("ast2", "ast-transformed", false, "print the AST after transformations, before code generation (debug)");
		options.addOption("pp", "pretty-print", false, "pretty print the AST before code generation (debug)");
		options.addOption("cd", "compilation-dir", true, "directory to store all generated files");
		options.addOption("views", "find-external-views", false, "print all referenced external view names");
		options.addOption("viewSrcPath", "view-src-path", true, "view id and its src path");
		options.addOption("viewId", "view-id", true, "view name and its job id");
		options.addOption("jardir", "jar-dir", true, "the name of jar directory");
		options.addOption("wfdir", "workflow-dir", true, "the name of workflow directory");
		options.addOption("job", "job", true, "sets the MySql ID to update with this job's status");

		final CommandLine cl;
		try {
			cl = new PosixParser().parse(options, args);
		} catch (final org.apache.commons.cli.ParseException e) {
			System.err.println(e.getMessage());
			new HelpFormatter().printHelp("Boa Compiler", options);
			return null;
		}

		// get the filename of the program we will be compiling
		inputFiles = new ArrayList<File>();
		if (cl.hasOption('i')) {
			final String[] inputPaths = cl.getOptionValue('i').split(",");

			for (final String s : inputPaths) {
				final File f = new File(s);
				if (!f.exists())
					System.err.println("File '" + s + "' does not exist, skipping");
				else
					inputFiles.add(new File(s));
			}
		}

		if (inputFiles.size() == 0) {
			System.err.println("no valid input files found - did you use the --in option?");
			//new HelpFormatter().printHelp("BoaCompiler", options);
			new HelpFormatter().printHelp("Boa Compiler", options);
			return null;
		}

		return cl;
	}

	private static CommandLine processParseCommandLineOptions(final String[] args) {
		// parse the command line options
		final Options options = new Options();
		options.addOption("l", "libs", true, "extra jars (functions/aggregators) to be compiled in");
		options.addOption("i", "in", true, "file(s) to be parsed (comma-separated list)");

		final CommandLine cl;
		try {
			cl = new PosixParser().parse(options, args);
		} catch (final org.apache.commons.cli.ParseException e) {
			printHelp(options, e.getMessage());
			return null;
		}

		// get the filename of the program we will be compiling
		inputFiles = new ArrayList<File>();
		if (cl.hasOption('i')) {
			final String[] inputPaths = cl.getOptionValue('i').split(",");

			for (final String s : inputPaths) {
				final File f = new File(s);
				if (!f.exists())
					System.err.println("File '" + s + "' does not exist, skipping");
				else
					inputFiles.add(new File(s));
			}
		}

		if (inputFiles.size() == 0) {
			printHelp(options, "no valid input files found - did you use the --in option?");
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
			className = "";
			for (final File f : inputFiles) {
				if (className.length() != 0)
					className += "_";
				className += jarToClassname(f);
			}
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

	private static void generateJar(final String jarName, final File dir, final File jarDir, final List<File> libJars) throws IOException, FileNotFoundException {
		final JarOutputStream jar = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(new File(jarDir, "Query.jar"))));

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

	private static void generateWorkflow(final String jobName, final ViewFindingVisitor vfv, final List<String> javaArgs, final File dir) throws IOException {
		final List<String> wfViews = new ArrayList<String>();
		final List<String> wfPaths = new ArrayList<String>();
		String outputPath = jobName.replaceAll("-", "/");
		Set<String> localSubViewNames = vfv.getLocalSubViewNames();

		List<String> referencedOutputs = vfv.getReferencedOutputs();
		for (int i  = 0; i < referencedOutputs.size(); i++) {
			String head = referencedOutputs.get(i).split("/")[0];
			String name = (localSubViewNames.contains(head) || head.equals("output")) ? outputPath : String.valueOf(jobId);
			referencedOutputs.set(i, name + "/" + referencedOutputs.get(i));
		}

		List<String> internalViews = vfv.getLocalSubViews();
		for (String svPath : internalViews) {
			String head = svPath.split("/")[0];
			String name = localSubViewNames.contains(head) ? jobName : String.valueOf(jobId);
			wfViews.add(name + "-" + svPath.replaceAll("/", "-"));
			wfPaths.add(name.replaceAll("-", "/") + "/" + svPath);
		}

		List<String> localExternalViews = vfv.getLocalExternalViews();
		List<String> localExternalSubViewPaths = vfv.getLocalExternalSubViewPaths();
		for (int i = 0; i < localExternalViews.size(); i++) {
			String view = localExternalViews.get(i);
			String subViewPath = localExternalSubViewPaths.get(i);
			String viewId;
			if (view.contains("/") && viewIds.containsKey(view) && viewSrcPaths.containsKey(view))
				viewId = viewIds.get(view);
			else if (!view.contains("/") && viewSrcPaths.containsKey(view))
				viewId = view;
			else
				throw new IOException("unable to create workflow for external view " + view);

			if (subViewPath.equals(""))
				wfPaths.add(viewId);
			else {
				wfPaths.add(viewId + "/" + subViewPath);
				view += "-" + subViewPath.replaceAll("/", "-");
			}
			wfViews.add(view);
		}

		final BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(new File(dir, "workflow.xml")));
		final WorkflowGenerator wg = new WorkflowGenerator(jobName, outputPath, referencedOutputs, wfViews, wfPaths, javaArgs);

		wg.createWorkflow();
		final String wf = wg.getWorkflow();
		o.write(wf.getBytes());
		o.close();
	}

	private static Map<String, String> generateViewIds(final CommandLine cl) {
		final Map<String, String> viewIds = new HashMap<String, String>();

		if (!cl.hasOption("viewId"))
			return viewIds;

		for (final String viewId : cl.getOptionValues("viewId")) {
			final String[] ary = viewId.split(":");
			if (!viewIds.containsKey(ary[0]))
				viewIds.put(ary[0], ary[1]);
		}

		return viewIds;
	}

	private static Map<String, String> generateViewSrcPaths(final CommandLine cl) {
		final Map<String, String> viewSrcPaths = new HashMap<String, String>();

		if (!cl.hasOption("viewSrcPath"))
			return viewSrcPaths;

		for (final String srcPath : cl.getOptionValues("viewSrcPath")) {
			final String[] ary = srcPath.split(":");
			if (!viewSrcPaths.containsKey(ary[0]))
				viewSrcPaths.put(ary[0], ary[1]);
		}

		return viewSrcPaths;
	}

	private static Map<String, Start> generateViewASTs(final CommandLine cl) {
		final Map<String, Start> viewASTs = new HashMap<String, Start>();

		if (!cl.hasOption("viewSrcPath"))
			return viewASTs;

		for (final String srcPath : cl.getOptionValues("viewSrcPath")) {
			final String[] ary = srcPath.split(":");
			final String currentFilePath = ary[1];
			try {
				if (!viewASTs.containsKey(ary[0])) {
					final BoaLexer lexer = new BoaLexer(new ANTLRFileStream(currentFilePath));
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

					viewASTs.put(ary[0], p);
				}
			} catch (final Exception e) {
				System.err.print(currentFilePath + ": compilation failed: ");
				e.printStackTrace();
			}
		}

		return viewASTs;
	}
}
