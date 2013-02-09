package boa.compiler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.antlr.stringtemplate.StringTemplateGroup;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

import org.scannotation.ClasspathUrlFinder;

import boa.parser.ParseException;
import boa.parser.BoaParser;
import boa.parser.syntaxtree.Program;

public class BoaCompiler {
	private static Logger LOG = Logger.getLogger(BoaCompiler.class);

	public static void main(final String[] args) throws IOException, ParseException {
		// parse the command line options
		final Options options = new Options();
		options.addOption("h", "hadoop-base", true, "base directory for Hadoop installation");
		options.addOption("l", "libs", true, "extra jars (functions/aggregators) to be compiled in");
		options.addOption("i", "in", true, "file to be compiled");
		options.addOption("o", "out", true, "the name of the resulting jar");
		options.addOption("n", "name", true, "the name of the generated main class");

		CommandLine cl;
		try {
			cl = new PosixParser().parse(options, args);
		} catch (final org.apache.commons.cli.ParseException e) {
			System.err.println(e.getMessage());

			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("BoaCompiler", options);

			return;
		}

		// get the base of the hadoop installation for compilation purposes
		File hadoopBase;
		if (cl.hasOption('h')) {
			hadoopBase = new File(cl.getOptionValue('h'));
		} else {
			System.err.println("missing required option `hadoop-base'");

			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("BoaCompiler", options);

			return;
		}

		// find the location of the jar this class is in
		final String path = ClasspathUrlFinder.findClassBase(BoaCompiler.class).getPath();
		// find the location of the compiler distribution
		final String root = new File(path.substring(path.indexOf(':') + 1, path.indexOf('!'))).getParentFile().getParent();

		// get the filename of the program we will be compiling
		if (!cl.hasOption('i')) {
			System.err.println("missing required option `in'");

			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("BoaCompiler", options);

			return;
		}

		final File inputFile = new File(cl.getOptionValue('i'));

		final String className;
		if (cl.hasOption('n'))
			className = cl.getOptionValue('n');
		else
			className = pascalCase(inputFile.getName().substring(0, inputFile.getName().lastIndexOf('.')));

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
			
			final BufferedReader s = new BufferedReader(new InputStreamReader(CodeGeneratingVisitor.class.getClassLoader().getResource("BoaJavaHbase.stg").openStream()));
			try {
				stg = new StringTemplateGroup(s);
				stg.setSuperGroup(superStg);
			} finally {
				s.close();
			}

			final BufferedReader r = new BufferedReader(new FileReader(inputFile));
			
			try {
				new BoaParser(r);
				final Program p = BoaParser.Start().f0;

				final TypeCheckingVisitor typeChecker = new TypeCheckingVisitor();
				typeChecker.visit(p, new SymbolTable(libs));

				final String src = new CodeGeneratingVisitor(typeChecker, className, stg).visit(p);

				o.write(src.getBytes());
			} finally {
				r.close();
			}
		} finally {
			o.close();
		}

		final String runtimePath = root + "/dist/boa-runtime.jar";
		final StringBuilder classPath = new StringBuilder(runtimePath);

		final Matcher m = Pattern.compile("(hadoop-[a-z]+-[\\d+\\.]+|hbase-[\\d+\\.]+)\\.jar").matcher("");
		for (final File f : hadoopBase.listFiles())
			if (m.reset(f.getName()).matches())
				classPath.append(":" + f);
		for (final File f : new File(hadoopBase, "lib").listFiles())
			if (f.toString().endsWith(".jar"))
				classPath.append(":" + f);

		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		BoaCompiler.LOG.info("compiling " + outputFile);
		if (compiler.run(null, null, null, "-cp", classPath.toString(), outputFile.toString()) != 0)
			throw new RuntimeException("compile failed");

		generateJar(cl, jarName, outputRoot, runtimePath);

		BoaCompiler.delete(outputRoot);
	}

	private static final void delete(final File f) throws IOException {
		if (f.isDirectory())
			for (final File g : f.listFiles())
				BoaCompiler.delete(g);

		if (!f.delete())
			throw new IOException("unable to delete file " + f);
	}

	private static void generateJar(CommandLine cl, String jarName, final File dir, final String runtimePath) throws IOException, FileNotFoundException {
		final JarOutputStream jar = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(new File(jarName))));
		try {
			final int offset = dir.toString().length() + 1;
			for (final String f : BoaCompiler.findFiles(dir, new ArrayList<String>())) {
				BoaCompiler.LOG.info("adding " + f + " to " + jarName);
				jar.putNextEntry(new ZipEntry(f.substring(offset)));

				final InputStream inx = new BufferedInputStream(new FileInputStream(f));
				try {
					BoaCompiler.write(inx, jar);
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

				BoaCompiler.LOG.info("adding lib/" + f.getName() + " to " + jarName);
				jar.putNextEntry(new JarEntry("lib" + File.separatorChar + f.getName()));
				final InputStream inx = new BufferedInputStream(new FileInputStream(f));
				try {
					BoaCompiler.write(inx, jar);
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
				BoaCompiler.findFiles(g, l);
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
