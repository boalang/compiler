/*
 * Copyright 2016, Hridesh Rajan, Robert Dyer,
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
package boa.test.datagen;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.protobuf.format.JsonFormat;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import static org.junit.Assert.assertEquals;

import boa.types.Ast.ASTRoot;

import boa.datagen.util.Java7Visitor;

import boa.test.compiler.BaseTest;

/*
 * @author rdyer
 */
public class Java7BaseTest extends BaseTest {
    @SuppressWarnings("deprecation")
	protected static int astLevel = AST.JLS4;
	protected static String javaVersion = JavaCore.VERSION_1_7;
	protected static Java7Visitor visitor = new Java7Visitor("", new HashMap<String, Integer>());

	protected static void dumpJavaWrapped(final String content) {
        dumpJava(getWrapped(content));
    }

	protected static void dumpJava(final String content) {
		final ASTParser parser = ASTParser.newParser(astLevel);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(content.toCharArray());

		final Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(javaVersion, options);
		parser.setCompilerOptions(options);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        try {
            final UglyMathCommentsExtractor cex = new UglyMathCommentsExtractor(cu, content);
            final ASTDumper dumper = new ASTDumper(cex);
            dumper.dump(cu);
            cex.close();
        } catch (final Exception e) {}
    }

	protected static String parseJava(final String content) {
		final StringBuilder sb = new StringBuilder();
		final FileASTRequestor r = new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {
				final ASTRoot.Builder ast = ASTRoot.newBuilder();
				try {
					ast.addNamespaces(visitor.getNamespaces(cu));
					for (final String s : visitor.getImports())
						ast.addImports(s);
				} catch (final Exception e) {
					System.err.println(e);
					e.printStackTrace();
				}

				sb.append(JsonFormat.printToString(ast.build()));
			}
		};
		Map<String, String> fileContents = new HashMap<String, String>();
		fileContents.put("", content);
		@SuppressWarnings("rawtypes")
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setCompilerOptions(options);
		parser.setEnvironment(new String[0], new String[]{}, new String[]{}, true);
		parser.setResolveBindings(true);
		parser.createASTs(fileContents, new String[]{""}, null, new String[0], r, null);
		
		return sb.toString();
	}

	protected static String getWrapped(final String content) {
		String s = "class t {\n   void m() {\n      " + content.replaceAll("\n", "\n      ");
		if (!content.endsWith(";") && !content.endsWith(";\n"))
			s += ";";
		s += "\n   }\n}";
        return s;
	}

	protected static String parseWrapped(final String content) {
		return parseJava(getWrapped(content));
    }

	public static void testWrapped(final String java, final String expected) {
		assertEquals(
			"{\n" +
			"   \"namespaces\": [\n" +
			"      {\n" +
			"         \"name\": \"\",\n" +
			"         \"declarations\": [\n" +
			"            {\n" +
			"               \"name\": \"t\",\n" +
			"               \"kind\": \"CLASS\",\n" +
			"               \"methods\": [\n" +
			"                  {\n" +
			"                     \"name\": \"m\",\n" +
			"                     \"return_type\": {\n" +
			"                        \"name\": \"void\",\n" +
			"                        \"kind\": \"OTHER\"\n" +
			"                     },\n" +
			"                     \"statements\": [\n" +
			"                        {\n" +
			"                           \"kind\": \"BLOCK\",\n" +
			"                           \"statements\": [\n" +
			"                              " + expected.replaceAll("\n", "\n                              ") + "\n" +
			"                           ]\n" +
			"                        }\n" +
			"                     ]\n" +
			"                  }\n" +
			"               ]\n" +
			"            }\n" +
			"         ]\n" +
			"      }\n" +
			"   ]\n" +
			"}",
			parseWrapped(java)
		);
	}
}
