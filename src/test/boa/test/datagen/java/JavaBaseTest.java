/*
 * Copyright 2016-2022, Hridesh Rajan, Robert Dyer, Huaiyao Ma,
 *                 Iowa State University of Science and Technology,
 *                 Bowling Green State University,
 *                 and University of Nebraska Board of Regents
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
package boa.test.datagen.java;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import com.googlecode.protobuf.format.JsonFormat;

import boa.datagen.util.FileIO;
import boa.datagen.util.JavaVisitor;
import boa.functions.langmode.JavaLangMode;
import boa.test.compiler.BaseTest;
import boa.types.Ast.ASTRoot;


/*
 * @author rdyer
 * @author huaiyao
 */
public class JavaBaseTest extends BaseTest {
	protected static int astLevel = JavaLangMode.DEFAULT_JAVA_ASTLEVEL;
	protected static String javaVersion = JavaLangMode.DEFAULT_JAVA_CORE;
	protected static final JavaVisitor visitor = new JavaVisitor("");

	protected static void setJavaVersion() {
		astLevel = JavaLangMode.DEFAULT_JAVA_ASTLEVEL;
		javaVersion = JavaLangMode.DEFAULT_JAVA_CORE;
	}

	protected static void dumpJava(final String content) {
		setJavaVersion();
		final ASTParser parser = ASTParser.newParser(astLevel);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(content.toCharArray());

		final Map<String, String> options = (Map<String, String>) JavaCore.getOptions();
		JavaCore.setComplianceOptions(javaVersion, options);
		parser.setCompilerOptions(options);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		try {
			try (final UglyMathCommentsExtractor cex = new UglyMathCommentsExtractor(cu, content)) {
				new ASTDumper(cex).dump(cu);
			}
		} catch (final Exception e) {}
	}

	public static String parseJava(final String content) {
		final File f = new File(new File(System.getProperty("java.io.tmpdir")), UUID.randomUUID().toString());
		try {
			FileIO.writeFileContents(f, content);
			return parseJavaFile(f.getPath());
		} finally {
			try {
				FileIO.delete(f);
			} catch (final Exception e) {}
		}
	}

	public static String parseJavaFile(final String path) {
		setJavaVersion();
		final StringBuilder sb = new StringBuilder();
		final FileASTRequestor r = new FileASTRequestor() {
			@Override
			public void acceptAST(final String sourceFilePath, final CompilationUnit cu) {
				final ASTRoot.Builder ast = ASTRoot.newBuilder();
				try {
					visitor.reset("");
					ast.addNamespaces(visitor.getNamespaces(cu));
				} catch (final Exception e) {
					System.err.println(e);
					e.printStackTrace();
				}

				sb.append(JsonFormat.printToString(ast.build()));
			}
		};
		final Map<String, String> fileContents = new HashMap<String, String>();
		@SuppressWarnings("rawtypes")
		final Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, javaVersion);
		options.put(JavaCore.COMPILER_SOURCE, javaVersion);
		final ASTParser parser = ASTParser.newParser(astLevel);
		parser.setCompilerOptions(options);
		parser.setEnvironment(new String[0], new String[]{}, new String[]{}, true);
		parser.setResolveBindings(true);
		parser.createASTs(new String[] { path }, null, new String[0], r, null);

		return FileIO.normalizeEOL(sb.toString());
	}

	protected static void dumpJavaWrapped(final String content) {
		setJavaVersion();
		dumpJava(getWrapped(content));
	}

	protected static String parseWrapped(final String content) {
		setJavaVersion();
		return parseJava(getWrapped(content));
	}

	protected static String getWrapped(final String content) {
		String s = "class t {\n   void m() {\n      " + content.replaceAll("\n", "\n      ").trim();
		if (content.indexOf(";") == -1 && !s.endsWith("}"))
			s += ";";
		if (!s.endsWith("\n"))
			s += "\n";
		s += "   }\n}";
		return s;
	}

	protected static String getWrappedResult(final String expected) {
		return "{\n"
				+ "   \"namespaces\": [\n"
				+ "      {\n"
				+ "         \"name\": \"\",\n"
				+ "         \"declarations\": [\n"
				+ "            {\n"
				+ "               \"name\": \"t\",\n"
				+ "               \"kind\": \"CLASS\",\n"
				+ "               \"methods\": [\n"
				+ "                  {\n"
				+ "                     \"name\": \"m\",\n"
				+ "                     \"return_type\": {\n"
				+ "                        \"name\": \"void\",\n"
				+ "                        \"kind\": \"PRIMITIVE\"\n"
				+ "                     },\n"
				+ "                     \"statements\": [\n"
				+ "                        {\n"
				+ "                           \"kind\": \"BLOCK\",\n"
				+ "                           \"statements\": [\n"
				+ "                              " + expected.replaceAll("\n", "\n                              ") + "\n"
				+ "                           ]\n"
				+ "                        }\n"
				+ "                     ]\n"
				+ "                  }\n"
				+ "               ],\n"
				+ "               \"fully_qualified_name\": \"t\"\n"
				+ "            }\n"
				+ "         ]\n"
				+ "      }\n"
				+ "   ]\n"
				+ "}";
	}
}
