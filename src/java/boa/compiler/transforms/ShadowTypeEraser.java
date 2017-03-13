/*
 * Copyright 2017, Hridesh Rajan, Robert Dyer, Kaushik Nimmala
 *                 Iowa State University of Science and Technology
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
package boa.compiler.transforms;

import java.util.*;

import boa.compiler.ast.Factor;
import boa.compiler.ast.Selector;
import boa.compiler.ast.Term;
import boa.compiler.ast.Node;
import boa.compiler.ast.Component;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.Call;
import boa.compiler.ast.expressions.*;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.Statement;
import boa.compiler.SymbolTable;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.types.BoaShadowType;
import boa.types.BoaTuple;
import boa.types.proto.StatementProtoTuple;

/**
 * Converts a tree using shadow types into a tree without shadow types.
 *
 * @author rdyer
 * @author kaushin
 */
public class ShadowTypeEraser extends AbstractVisitorNoArg {
	private LinkedList<Expression> expressionStack = new LinkedList<Expression>();
	private boolean flag = false;
	private List<Node> ops = null;

	// track nearest Expression node
	public void visit(final Expression n) {
		expressionStack.push(n);
		super.visit(n);
		expressionStack.pop();
	}

	@Override
	public void visit(final Factor n) {
		flag = false;
		ops = n.getOps();
		super.visit(n);
	}

	// replacing shadow type selectors
	@Override
	public void visit(final Selector n) {
		super.visit(n);

		final Factor fact = (Factor)n.getParent();

		if (!flag && fact.getOperand().type instanceof BoaShadowType) {
			// avoid replacing past the first selector
			flag = true;
			final Expression parentExp = expressionStack.peek();

			// get shadow type used
			final Identifier id = (Identifier)fact.getOperand();
			final BoaShadowType shadow = (BoaShadowType)fact.getOperand().type;

			// replace the selector
			final Expression replacement = (Expression)shadow.lookupCodegen(n.getId().getToken(), id.getToken(), parentExp.env);
			final ParenExpression paren = new ParenExpression(replacement);
			final Factor newFact = new Factor(paren);
			final Expression newExp = ASTFactory.createFactorExpr(newFact);

			if (ops != null)
				for (int i = 1; i < ops.size(); i++)
					newFact.addOp(ops.get(i));

			newFact.env = parentExp.env;
			paren.type = replacement.type;
			newExp.type = paren.type;

			parentExp.replaceExpression(parentExp, newExp);
		}
	}

	// removing shadow types in before/after visit
	@Override
	public void visit(final Component n) {
		super.visit(n);

		if (n.type instanceof BoaShadowType) {
			final BoaShadowType shadow = (BoaShadowType)n.type;

			// change the identifier
			final Identifier id = (Identifier)n.getType();
			id.setToken(shadow.shadowedName());

			// update types
			n.type = n.getType().type = shadow.shadowedType;
			n.env.set(n.getIdentifier().getToken(), n.type);
		}
	}

	// removing shadow types in variable declarations
	@Override
	public void visit(final VarDeclStatement n) {
		super.visit(n);

		if (n.hasType()) {
			if (n.type instanceof BoaShadowType) {
				final BoaShadowType shadow = (BoaShadowType)n.env.get(n.getType().toString());

				// change the identifier
				final Identifier id = (Identifier)n.getType();
				id.setToken(shadow.shadowedName());

				// update types
				n.type = shadow.shadowedType;
				n.env.setType(n.getId().getToken(), shadow.shadowedType);
			}
		}
	}
}
