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
package boa.types.shadow;

import boa.compiler.ast.Call;
import boa.compiler.ast.Comparison;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Index;
import boa.compiler.ast.literals.IntegerLiteral;
import boa.compiler.ast.Node;
import boa.compiler.ast.Operand;
import boa.compiler.ast.Selector;
import boa.compiler.ast.Term;
import boa.compiler.SymbolTable;
import boa.compiler.transforms.ASTFactory;
import boa.types.BoaInt;
import boa.types.BoaProtoList;
import boa.types.BoaShadowType;
import boa.types.proto.enums.StatementKindProtoMap;
import boa.types.proto.ExpressionProtoTuple;
import boa.types.proto.StatementProtoTuple;

/**
 * A shadow type for IfStatement.
 * 
 * @author rdyer
 * @author kaushin
 */
public class IfStatementShadow extends BoaShadowType  {
	/**
	 * Construct a {@link IfStatementShadow}.
	 */
	public IfStatementShadow() {
		super(new StatementProtoTuple());

		addShadow("condition", new ExpressionProtoTuple());
		addShadow("true_branch", new StatementProtoTuple());
		addShadow("false_branch", new StatementProtoTuple());
	}

	/** {@inheritDoc} */
	@Override
	public Node lookupCodegen(final String name, final String nodeId, final SymbolTable env) {
		final Identifier id = ASTFactory.createIdentifier(nodeId, env);
		id.type = new StatementProtoTuple();

		if ("condition".equals(name)) {
			// ${0}.expression
			final Selector s = new Selector(ASTFactory.createIdentifier("expression", env));
			final Factor f = new Factor(id).addOp(s);
			final Expression tree = ASTFactory.createFactorExpr(f);

			s.env = f.env = env;

			s.type = f.type = tree.type = new ExpressionProtoTuple();

			return tree;
		}

		if ("true_branch".equals(name)) {
			// ${0}.statements[0]
			final Selector s = new Selector(ASTFactory.createIdentifier("statements", env));
			final Index idx = new Index(ASTFactory.createFactorExpr(new IntegerLiteral("0")));
			final Factor f = new Factor(id).addOp(s).addOp(idx);
			final Expression tree = ASTFactory.createFactorExpr(f);

			s.env = f.env = idx.env = env;

			idx.getStart().type = new BoaInt();
			s.type = new BoaProtoList(new StatementProtoTuple());
			f.type = tree.type = new StatementProtoTuple();

			return tree;
		}

		if ("false_branch".equals(name)) {
			// (len(${0}.statements) > 1 ? null : ${0}.statements[1])
			final Selector s = new Selector(ASTFactory.createIdentifier("statements", env));
			final Factor f = new Factor(id).addOp(s);
			final Expression tree = ASTFactory.createFactorExpr(f);
			final Expression c = ASTFactory.createCallExpr("trinary", env, new StatementProtoTuple(), tree);

			s.env = f.env = env;

			s.type = f.type = tree.type = new BoaProtoList(new StatementProtoTuple());

			return c;
		}

		throw new RuntimeException("invalid shadow field: " + name);
	}

	/** {@inheritDoc} */
	@Override
	public Expression getKindExpression(final SymbolTable env) {
		return getKindExpression("StatementKind", "IF", new StatementKindProtoMap(), env);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "IfStatement";
	}
}
