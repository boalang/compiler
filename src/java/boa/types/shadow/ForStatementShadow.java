/*
 * Copyright 2017, Robert Dyer, Kaushik Nimmala
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
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.SymbolTable;
import boa.compiler.transforms.ASTFactory;
import boa.types.BoaInt;
import boa.types.BoaProtoList;
import boa.types.BoaShadowType;
import boa.types.proto.enums.StatementKindProtoMap;
import boa.types.proto.ExpressionProtoTuple;
import boa.types.proto.StatementProtoTuple;

/**
 * A shadow type for ForStatement.
 * 
 * @author rdyer
 * @author kaushin
 */
public class ForStatementShadow extends BoaShadowType  {
	/**
	 * Construct a {@link ForStatementShadow}.
	 */
	public ForStatementShadow() {
		super(new StatementProtoTuple());

		addShadow("initializers", new BoaProtoList(new ExpressionProtoTuple()));
		addShadow("expression", new ExpressionProtoTuple());
		addShadow("updates", new BoaProtoList(new ExpressionProtoTuple()));
		addShadow("body", new StatementProtoTuple());
	}

	/** {@inheritDoc} */
	@Override
	public Node lookupCodegen(final String name, final String nodeId, final SymbolTable env) {
		final Identifier id = ASTFactory.createIdentifier(nodeId, env);
		id.type = new StatementProtoTuple();

		if ("initializers".equals(name)) {
			// ${0}.initializations
			return ASTFactory.createSelector(id, "initializations", new BoaProtoList(new ExpressionProtoTuple()), new BoaProtoList(new ExpressionProtoTuple()), env);
		}

		if ("expression".equals(name)) {
			// ${0}.expression
			return ASTFactory.createSelector(id, "expression", new ExpressionProtoTuple(), new ExpressionProtoTuple(), env);
		}

		if ("updates".equals(name)) {
			// ${0}.updates
			return ASTFactory.createSelector(id, "updates", new BoaProtoList(new ExpressionProtoTuple()), new BoaProtoList(new ExpressionProtoTuple()), env);
		}

		if ("body".equals(name)) {
			// ${0}.statements
			final Expression tree = ASTFactory.createSelector(id, "statements", new BoaProtoList(new StatementProtoTuple()), new StatementProtoTuple(), env);
			// ${0}.statements[0]
			ASTFactory.getFactorFromExp(tree).addOp(ASTFactory.createIndex(ASTFactory.createIntLiteral(0), env));

			return tree;
		}

		throw new RuntimeException("invalid shadow field: " + name);
	}

	/** {@inheritDoc} */
	@Override
	public Expression getKindExpression(final SymbolTable env) {
		return getKindExpression("StatementKind", "FOR", new StatementKindProtoMap(), env);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ForStatement";
	}
}
