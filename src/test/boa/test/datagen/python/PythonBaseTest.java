/*
 * Copyright 2021, Robert Dyer,
 *                 University of Nebraska--Lincoln
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
package boa.test.datagen.python;

import com.googlecode.protobuf.format.JsonFormat;

import boa.types.Ast.ASTRoot;
import boa.datagen.util.FileIO;
import boa.datagen.util.NewPythonVisitor;
import boa.test.compiler.BaseTest;

import org.eclipse.dltk.python.internal.core.parser.PythonSourceParser;
import org.eclipse.dltk.python.parser.ast.PythonModuleDeclaration;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.compiler.env.ModuleSource;
import org.eclipse.dltk.compiler.problem.IProblemReporter;
import org.eclipse.dltk.compiler.problem.IProblem;

/*
 * @author rdyer
 */
public class PythonBaseTest extends BaseTest {
    private static boolean pythonParsingError = false;

	protected static String parsePython(final String content) {
		final StringBuilder sb = new StringBuilder();

		pythonParsingError = false;

		PythonSourceParser parser = new PythonSourceParser();
		IModuleSource input = new ModuleSource(content);

		IProblemReporter reporter = new IProblemReporter() {
			@Override
			public void reportProblem(IProblem arg0) {
				pythonParsingError = true;
			}
		};

        final ASTRoot.Builder ast = ASTRoot.newBuilder();
        NewPythonVisitor visitor = new NewPythonVisitor();
        visitor.enableDiff = false;

        try {
            PythonModuleDeclaration module = (PythonModuleDeclaration) parser.parse(input, reporter);
            ast.addNamespaces(visitor.getNamespace(module, ""));
        } catch (final Throwable e) {
            return "";
        }

        sb.append(JsonFormat.printToString(ast.build()));
		return FileIO.normalizeEOL(sb.toString());
	}
}
