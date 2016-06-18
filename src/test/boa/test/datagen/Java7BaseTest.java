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

import static org.junit.Assert.assertEquals;

import boa.types.Ast.ASTRoot;

import boa.datagen.util.Java7Visitor;

import boa.test.compiler.BaseTest;

/*
 * @author rdyer
 */
public class Java7BaseTest extends BaseTest {
	protected static int astLevel = AST.JLS4;
	protected static String javaVersion = JavaCore.VERSION_1_7;
	protected static Java7Visitor visitor = new Java7Visitor("", new HashMap<String, Integer>());

	protected static String parseJava(final String content) {
		final ASTParser parser = ASTParser.newParser(astLevel);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(content.toCharArray());

		final Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(javaVersion, options);
		parser.setCompilerOptions(options);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		final ASTRoot.Builder ast = ASTRoot.newBuilder();
		try {
			ast.addNamespaces(visitor.getNamespaces(cu));
			for (final String s : visitor.getImports())
				ast.addImports(s);
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
			return "";
		}

		return JsonFormat.printToString(ast.build());
	}

	protected static String parseWrapped(final String content) {
		String s = "class t {\n   void m() {\n      " + content.replaceAll("\n", "\n      ");
		if (!content.endsWith(";") && !content.endsWith(";\n"))
			s += ";";
		s += "\n   }\n}";
		return parseJava(s);
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
			"                        \"kind\": \"OTHER\",\n" +
			"                        \"name\": 0\n" +
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
