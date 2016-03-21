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

import java.io.*;
import java.util.*;

import com.googlecode.protobuf.format.JsonFormat;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import boa.types.Ast.ASTRoot;

import boa.datagen.util.Java7Visitor;
import boa.datagen.util.Java8Visitor;

/*
 * @author rdyer
 */
public class BaseTest {
	public static String parse(final String content) {
		final ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(content.toCharArray());

		final Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		final ASTRoot.Builder ast = ASTRoot.newBuilder();
		final Java7Visitor visitor = new Java8Visitor(content, new HashMap<String, Integer>());
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

	public static String parseWrapped(final String content) {
		String s = "class t {\n\tvoid m() {\n\t\t" + content;
		if (!content.endsWith(";"))
			s += ";\n";
		s += "\n\t}\n}";
		return parse(s);
	}
}
