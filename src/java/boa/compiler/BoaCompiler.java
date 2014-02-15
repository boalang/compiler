package boa.compiler;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

import org.scannotation.ClasspathUrlFinder;

import boa.compiler.ast.Program;
import boa.compiler.ast.Start;
import boa.compiler.transforms.LocalAggregationTransformer;
import boa.compiler.transforms.VisitorMergingTransformer;
import boa.compiler.transforms.VisitorOptimizingTransformer;
import boa.compiler.visitors.CodeGeneratingVisitor;
import boa.compiler.visitors.TaskClassifyingVisitor;
import boa.compiler.visitors.TypeCheckingVisitor;
import boa.parser.ParseException;
import boa.parser.BoaParser;

/**
 * The main entry point for the Boa compiler.
 *
 * @author anthonyu
 * @author rdyer
 */
public class BoaCompiler {
	private static Logger LOG = Logger.getLogger(BoaCompiler.class);

	public static void main(final String[] args) throws IOException, ParseException {
		// parse the command line options
		final Options options = new Options();
		options.addOption("l", "libs", true, "extra jars (functions/aggregators) to be compiled in");
		options.addOption("i", "in", true, "file(s) to be compiled (comma-separated list)");
		options.addOption("o", "out", true, "the name of the resulting jar");
		options.addOption("b", "hbase", false, "use HBase templates");
		options.addOption("nv", "no-visitor-fusion", false, "disable visitor fusion");
		options.addOption("v", "visitors-fused", true, "number of visitors to fuse");
		options.addOption("n", "name", true, "the name of the generated main class");

		final CommandLine cl;
		try {
			cl = new PosixParser().parse(options, args);
		} catch (final org.apache.commons.cli.ParseException e) {
			System.err.println(e.getMessage());
			new HelpFormatter().printHelp("BoaCompiler", options);

			return;
		}

		// get the filename of the program we will be compiling
		final ArrayList<File> inputFiles = new ArrayList<File>();
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
			System.err.println("missing required option `in'");
			new HelpFormatter().printHelp("BoaCompiler", options);

			return;
		}


		// get the name of the generated class
		final String className;
		if (cl.hasOption('n')) {
			className = cl.getOptionValue('n');
		} else {
			String s = "";
			for (final File f : inputFiles) {
				if (s.length() != 0)
					s += "_";
				if (f.getName().indexOf('.') != -1)
					s += pascalCase(f.getName().substring(0, f.getName().lastIndexOf('.')));
				else
					s += pascalCase(f.getName());
			}
			className = s;
		}

		// get the filename of the jar we will be writing
		final String jarName;
		if (cl.hasOption('o'))
			jarName = cl.getOptionValue('o');
		else
			jarName = className + ".jar";

		// make the output directory
		final File outputRoot = new File(new File(System.getProperty("java.io.tmpdir")), UUID.randomUUID().toString());
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
			final StringTemplateGroup superStg;
			final BufferedReader t = new BufferedReader(new InputStreamReader(CodeGeneratingVisitor.class.getClassLoader().getResource("BoaJava.stg").openStream()));
			try {
				superStg = new StringTemplateGroup(t);
			} finally {
				t.close();
			}

			final StringTemplateGroup stg;

			final String templateName;
			if (cl.hasOption('b'))
				templateName = "Hbase";
			else
				templateName = "Hadoop";
			final BufferedReader s = new BufferedReader(new InputStreamReader(CodeGeneratingVisitor.class.getClassLoader().getResource("BoaJava" + templateName + ".stg").openStream()));
			try {
				stg = new StringTemplateGroup(s);
				stg.setSuperGroup(superStg);
			} finally {
				s.close();
			}

			final List<String> jobnames = new ArrayList<String>();
			final List<String> jobs = new ArrayList<String>();
			BoaParser parser = null;
			boolean isSimple = true;

			final List<Program> visitorPrograms = new ArrayList<Program>();

			SymbolTable.initialize(libs);

			for (int i = 0; i < inputFiles.size(); i++) {
				final File f = inputFiles.get(i);
				final BufferedReader r = new BufferedReader(new FileReader(f));

				try {
					if (parser == null)
						parser = new BoaParser(r);
					else
						BoaParser.ReInit(r);
					parser.setTabSize(4);
					final Start p = BoaParser.Start();

					final String jobName = "" + i;

					final TypeCheckingVisitor typeChecker = new TypeCheckingVisitor();
					typeChecker.start(p, new SymbolTable());

					final TaskClassifyingVisitor simpleVisitor = new TaskClassifyingVisitor();
					simpleVisitor.start(p);

					LOG.info(f.getName() + ": task complexity: " + (!simpleVisitor.hasVisitor() ? "simple" : "complex"));
					isSimple &= !simpleVisitor.hasVisitor();

					new LocalAggregationTransformer().start(p);
						
					// if a job has no visitor, let it have its own method
					// also let jobs have own methods if visitor merging is disabled
					if (!simpleVisitor.hasVisitor() || cl.hasOption("nv") || inputFiles.size() == 1) {
						if (simpleVisitor.hasVisitor())
							new VisitorOptimizingTransformer().start(p);

						final CodeGeneratingVisitor cg = new CodeGeneratingVisitor(jobName, stg);
						cg.start(p);
						jobs.add(cg.getCode());

						jobnames.add(jobName);
					}
					// if a job has visitors, fuse them all together into a single program
					else {
						p.getProgram().jobName = jobName;
						visitorPrograms.add(p.getProgram());
					}
				} catch (final Exception e) {
					System.err.print(f.getName() + ": compilation failed: ");
					e.printStackTrace();
				} finally {
					r.close();
				}
			}

			final int maxVisitors;
			if (cl.hasOption('v'))
				maxVisitors = Integer.parseInt(cl.getOptionValue('v'));
			else
				maxVisitors = Integer.MAX_VALUE;

			if (!visitorPrograms.isEmpty())
				try {
					for (final Program p : new VisitorMergingTransformer().mergePrograms(visitorPrograms, maxVisitors)) {
						new VisitorOptimizingTransformer().start(p);
						final CodeGeneratingVisitor cg = new CodeGeneratingVisitor(p.jobName, stg);
						cg.start(p);
						jobs.add(cg.getCode());
		
						jobnames.add(p.jobName);
					}
				} catch (final Exception e) {
					System.err.println("error fusing visitors - falling back: " + e);
					e.printStackTrace();

					for (final Program p : visitorPrograms) {
						new VisitorOptimizingTransformer().start(p);

						final CodeGeneratingVisitor cg = new CodeGeneratingVisitor(p.jobName, stg);
						cg.start(p);
						jobs.add(cg.getCode());

						jobnames.add(p.jobName);
					}
				}

			if (jobs.size() == 0)
				throw new RuntimeException("no files compiled without error");

			final StringTemplate st = stg.getInstanceOf("Program");

			st.setAttribute("name", className);
			st.setAttribute("numreducers", inputFiles.size());
			st.setAttribute("jobs", jobs);
			st.setAttribute("jobnames", jobnames);
			st.setAttribute("combineTables", CodeGeneratingVisitor.combineTableStrings);
			st.setAttribute("reduceTables", CodeGeneratingVisitor.reduceTableStrings);
			st.setAttribute("splitsize", isSimple ? 64 * 1024 * 1024 : 10 * 1024 * 1024);

			o.write(st.toString().getBytes());
		} finally {
			o.close();
		}

		// compile the generated .java file
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		LOG.info("compiling: " + outputFile);
		LOG.info("classpath: " + System.getProperty("java.class.path"));
		if (compiler.run(null, null, null, "-cp", System.getProperty("java.class.path"), outputFile.toString()) != 0)
			throw new RuntimeException("compile failed");

		// find the location of the jar this class is in
		final String path = ClasspathUrlFinder.findClassBase(BoaCompiler.class).getPath();
		// find the location of the compiler distribution
		final String root = new File(path.substring(path.indexOf(':') + 1, path.indexOf('!'))).getParentFile().getParent();

		generateJar(cl, jarName, outputRoot, root + "/dist/boa-runtime.jar");

		delete(outputRoot);
	}

	private static final void delete(final File f) throws IOException {
		if (f.isDirectory())
			for (final File g : f.listFiles())
				delete(g);

		if (!f.delete())
			throw new IOException("unable to delete file " + f);
	}

	private static void generateJar(CommandLine cl, String jarName, final File dir, final String runtimePath) throws IOException, FileNotFoundException {
		final JarOutputStream jar = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(new File(jarName))));
		try {
			final int offset = dir.toString().length() + 1;
			for (final String f : findFiles(dir, new ArrayList<String>())) {
				//LOG.info("adding " + f + " to " + jarName);
				jar.putNextEntry(new ZipEntry(f.substring(offset)));

				final InputStream inx = new BufferedInputStream(new FileInputStream(f));
				try {
					write(inx, jar);
				} finally {
					inx.close();
				}

				jar.closeEntry();
			}

			final List<String> libsJars = new ArrayList<String>();
			libsJars.add(runtimePath);
			if (cl.hasOption('l'))
				libsJars.addAll(Arrays.asList(cl.getOptionValues('l')));

			for (final String lib : libsJars) {
				final File f = new File(lib);

				//LOG.info("adding lib/" + f.getName() + " to " + jarName);
				jar.putNextEntry(new JarEntry("lib" + File.separatorChar + f.getName()));
				final InputStream inx = new BufferedInputStream(new FileInputStream(f));
				try {
					write(inx, jar);
				} finally {
					inx.close();
				}
			}
		} finally {
			jar.close();
		}
	}

	private static final List<String> findFiles(final File f, final List<String> l) {
		if (f.isDirectory())
			for (final File g : f.listFiles())
				findFiles(g, l);
		else
			l.add(f.toString());

		return l;
	}

	private static void write(final InputStream in, final OutputStream out) throws IOException {
		final byte[] b = new byte[4096];
		int len;
		while ((len = in.read(b)) > 0)
			out.write(b, 0, len);
	}

	private static String pascalCase(final String string) {
		final StringBuilder pascalized = new StringBuilder();

		boolean lower = false;
		for (final char c : string.toCharArray())
			if (!Character.isDigit(c) && !Character.isLetter(c))
				lower = false;
			else if (Character.isDigit(c)) {
				pascalized.append(c);
				lower = false;
			} else if (Character.isLetter(c)) {
				if (lower)
					pascalized.append(c);
				else
					pascalized.append(Character.toUpperCase(c));

				lower = true;
			}

		return pascalized.toString();
	}
}
