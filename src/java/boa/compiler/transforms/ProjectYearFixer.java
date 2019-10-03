/*
 * Copyright 2019, Robert Dyer,
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

import java.util.Stack;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.ParenExpression;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.ast.Comparison;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.literals.IntegerLiteral;
import boa.compiler.ast.Node;
import boa.compiler.ast.Selector;
import boa.compiler.ast.Term;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.types.proto.ProjectProtoTuple;

/**
 * Fixes the bug with project creation years being off by 1000.
 *
 * @author rdyer
 */
public class ProjectYearFixer extends AbstractVisitorNoArg {
	/** {@inheritDoc} */
	@Override
	public void visit(final Factor n) {
		if (n.getOperand().type instanceof ProjectProtoTuple && n.getOps().size() == 1
				&& n.getOp(0) instanceof Selector && ((Selector)n.getOp(0)).getId().getToken().equals("created_date")) {
			n.setOperand(createParenExp(n));
			n.getOps().clear();
		} else {
			super.visit(n);
		}
	}

	private ParenExpression createParenExp(final Factor f) {
		final Term t = new Term(f.clone());
		t.addOp("/");
		t.addRhs(new Factor(new IntegerLiteral("1000")));

		return new ParenExpression(
			new Expression(
				new Conjunction(
					new Comparison(
						new SimpleExpr(t)
					)
				)
			)
		);
	}
}
