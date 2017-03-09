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

	// track nearest Expression node
	public void visit(final Expression n) {
		expressionStack.push(n);
		super.visit(n);
		expressionStack.pop();
	}

	// replacing shadow type selectors
	@Override
	public void visit(final Selector n) {
		super.visit(n);

		final Factor fact = (Factor)n.getParent();

		if (fact.getOperand().type instanceof BoaShadowType) {
			final Expression parentExp = expressionStack.peek();

			// get shadow type used
			final Identifier id = (Identifier)fact.getOperand();
			final BoaShadowType shadow = (BoaShadowType)fact.getOperand().type;

			// replace the selector
			// TODO I dont think this works for the cases like "ifstmt.true_branch.statements[0]"
			// i.e., anything where the selector is part of a more complex Factor with more ops after the selector
			final Expression replacement = (Expression)shadow.lookupCodegen(n.getId().getToken(), id.getToken(), parentExp.env);
			parentExp.replaceExpression(parentExp, replacement);
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
			id.setToken(shadow.getDeclarationIdentifierEraser);

			// update types
			n.type = n.getType().type = shadow.getDeclarationSymbolTableEraser;
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
				id.setToken(shadow.getDeclarationIdentifierEraser);

				// update types
				n.type = shadow.getDeclarationSymbolTableEraser;
				n.env.setType(n.getId().getToken(), shadow.getDeclarationSymbolTableEraser);
			}
		}
	}
}
