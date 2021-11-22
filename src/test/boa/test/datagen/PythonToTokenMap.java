/*
 * Copyright 2021, Robert Dyer, 
 *                 University of Nebraska Board of Regents
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

import org.eclipse.dltk.ast.ASTListNode;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.expressions.ExpressionList;
import org.eclipse.dltk.ast.statements.Block;
import org.eclipse.dltk.python.internal.core.parser.PythonSourceParser;
import org.eclipse.dltk.python.parser.ast.expressions.EmptyExpression;
import org.eclipse.dltk.python.parser.ast.PythonModuleDeclaration;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.compiler.env.ModuleSource;
import org.eclipse.dltk.compiler.problem.IProblemReporter;
import org.eclipse.dltk.compiler.problem.IProblem;

/*
 * @author rdyer
 */
public class PythonToTokenMap {
	public static void main(final String[] args) {
		for (final String s : args)
			parsePython(s);
	}

    public static class PythonVisitor extends ASTVisitor {
        private final String input;
        private final Map<Integer, Integer> lineMap;
        private final Map<Integer, Integer> colMap;

        public PythonVisitor(final String input, final Map<Integer, Integer> lineMap, final Map<Integer, Integer> colMap) {
            this.input = input;
            this.lineMap = lineMap;
            this.colMap = colMap;
        }

        private String escape(final String s) {
            return "\"" + s.replaceAll("\n", "\\\\n").replaceAll("\t", "\\\\t").replaceAll("\"", "\"\"") + "\"";
        }

        @Override
        public boolean visitGeneral(final ASTNode n) throws Exception {
            if (n.getClass() == PythonModuleDeclaration.class)
                return true;
            if (n.getClass() == Block.class)
                return true;
            if (n.getClass() == ASTListNode.class)
                return true;
            if (n.getClass() == ExpressionList.class)
                return true;
            if (n.getClass() == EmptyExpression.class)
                return true;

            System.err.print(escape(n.debugString().substring(0, n.debugString().indexOf("@"))));
            System.err.print(", " + escape(input.substring(n.start(), n.end())));
            System.err.print(", " + lineMap.get(n.start()));
            System.err.print(", " + lineMap.get(n.end() - 1));
            System.err.print(", " + colMap.get(n.start()));
            System.err.println(", " + colMap.get(n.end() - 1));

            return true;
        }
    }

    private static boolean pythonParsingError = false;

	protected static void parsePython(final String content) {
        final Map<Integer, Integer> lineMap = new HashMap<Integer, Integer>();
        final Map<Integer, Integer> colMap = new HashMap<Integer, Integer>();
        lineMap.put(-1, -1);
        colMap.put(-1, -1);

        int line = 1;
        int col = 1;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                line++;
                col = 0;
            }
            lineMap.put(i, line);
            colMap.put(i, col);
            col++;
        }

		final StringBuilder sb = new StringBuilder();

		pythonParsingError = false;

		final PythonSourceParser parser = new PythonSourceParser();
		final IModuleSource input = new ModuleSource(content);

		final IProblemReporter reporter = new IProblemReporter() {
			@Override
			public void reportProblem(final IProblem arg0) {
				pythonParsingError = true;
			}
		};

        try {
            final PythonModuleDeclaration module = (PythonModuleDeclaration)parser.parse(input, reporter);
            module.traverse(new PythonVisitor(content, lineMap, colMap));
        } catch (final Throwable e) { }
	}
}
