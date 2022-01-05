/*
 * Copyright 2022, Robert Dyer
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
package boa.compiler.transforms;

import boa.compiler.ast.Component;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.ast.Operand;
import boa.compiler.ast.expressions.FunctionExpression;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.types.FunctionType;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;
import boa.types.BoaFunction;
import boa.types.BoaName;
import boa.types.BoaType;

/**
 * Renames the arguments to functions then makes locals with their old name, so
 * they are assignable (not marked final). Later phases will handle things like
 * making a stack for them in recursive functions.
 *
 * @author rdyer
 */
public class LocalArgumentTransformer extends AbstractVisitorNoArgNoRet {
	/** {@inheritDoc} */
	@Override
	public void start(final Node n) {
		super.start(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionType t) {
		super.visit(t);

		if (t.getArgsSize() > 0)
			for (final Component c : t.getArgs()) {
				final String newName = "__arg_" + c.getIdentifier().getToken();

				c.getIdentifier().setToken(newName);
				c.env.set(newName, c.type);
			}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionExpression n) {
		super.visit(n);

		final BoaFunction t = (BoaFunction)n.type;

		if (t.countParameters() > 0) {
			final Block body = n.getBody();

			for (final BoaType p : t.getFormalParameters()) {
				// TODO rdyer - optimize, dont add this statement if the arg isnt used anywhere
				final BoaName c = (BoaName)p;
				final String name = c.getId();

				final Operand id = ASTFactory.createIdentifier("__arg_" + name, body.env);
				final VarDeclStatement stmt = ASTFactory.createVarDecl(name, id, c.getType(), body.env);

				if (body.getStatementsSize() > 0)
					body.insertStatementBefore(stmt, body.getStatement(0));
				else
					body.addStatement(stmt);
			}
		}
	}
}
