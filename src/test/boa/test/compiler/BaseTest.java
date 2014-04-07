package boa.test.compiler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.tools.ToolProvider;

import static org.junit.Assert.*;
import org.junit.BeforeClass;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

import org.stringtemplate.v4.ST;

import boa.compiler.SymbolTable;
import boa.compiler.ast.Start;
import boa.compiler.visitors.AbstractCodeGeneratingVisitor;
import boa.compiler.visitors.CodeGeneratingVisitor;
import boa.compiler.visitors.TypeCheckingVisitor;

import boa.parser.BoaLexer;
import boa.parser.BoaParser;

public abstract class BaseTest {
	@BeforeClass
	public static void setUp() throws IOException {
		SymbolTable.initialize(new ArrayList<URL>());
	}

	protected void codegen(final String input) throws IOException {
		codegen(input, null);
	}

	protected void codegen(final String input, final String error) throws IOException {
		final BoaParser parser = new BoaParser(new CommonTokenStream(new BoaLexer(new ANTLRInputStream(new StringReader(input)))));
		parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
		final Start p = parser.start().ast;

		final File outputRoot = new File(new File(System.getProperty("java.io.tmpdir")), UUID.randomUUID().toString());
		final File outputSrcDir = new File(outputRoot, "boa");
		if (!outputSrcDir.mkdirs())
			throw new IOException("unable to mkdir " + outputSrcDir);
		final File outputFile = new File(outputSrcDir, "Test.java");

		final List<String> jobnames = new ArrayList<String>();
		final List<String> jobs = new ArrayList<String>();

		try {
			new TypeCheckingVisitor().start(p, new SymbolTable());
			final CodeGeneratingVisitor cg = new CodeGeneratingVisitor("1");
			cg.start(p);
			jobs.add(cg.getCode());
			jobnames.add("1");

			final ST st = AbstractCodeGeneratingVisitor.stg.getInstanceOf("Program");

			st.add("name", "Test");
			st.add("numreducers", 1);
			st.add("jobs", jobs);
			st.add("jobnames", jobnames);
			st.add("combineTables", CodeGeneratingVisitor.combineTableStrings);
			st.add("reduceTables", CodeGeneratingVisitor.reduceTableStrings);
			st.add("splitsize", 64 * 1024 * 1024);

			final BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(outputFile));
			try {
				o.write(st.render().getBytes());
			} finally {
				o.close();
			}

			if (ToolProvider.getSystemJavaCompiler().run(null, null, null, "-cp", System.getProperty("java.class.path"), outputFile.toString()) != 0)
				throw new RuntimeException("compile failed");

			if (error != null)
				fail("expected error: " + error);
		} catch (final Exception e) {
			if (error == null)
				fail("found unexpected error: " + e.getMessage());
			else
				assertEquals(error, e.getMessage());
		}

		delete(outputSrcDir);
	}

	protected String load(final String fileName) throws IOException {
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

	protected final void delete(final File f) throws IOException {
		if (f.isDirectory())
			for (final File g : f.listFiles())
				delete(g);

		if (!f.delete())
			throw new IOException("unable to delete file " + f);
	}
}
