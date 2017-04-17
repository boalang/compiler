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
package boa.types;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Node;
import boa.compiler.ast.Selector;
import boa.compiler.SymbolTable;
import boa.compiler.transforms.ASTFactory;

/**
 * A shadow type.
 * 
 * @author rdyer
 * @author kaushin
 */
public abstract class BoaShadowType extends BoaTuple {
	public final BoaProtoTuple shadowedType;

	/**
	 * Construct a {@link BoaShadowType}.
	 *
	 * @param t the type being shadowed
	 */
	public BoaShadowType(final BoaProtoTuple t) {
		this.shadowedType = t;
	}

	/**
	 * Returns the name of the type being shadowed.
	 *
	 * @return the shadowed type's name
	 */
	public String shadowedName() {
		return shadowedType.toString();
	}

	/**
	 * Adds a shadowed attribute to this shadow type.
	 *
	 * @param name the name of the attribute to add to this shadow type
	 * @param t the type of the attribute
	 */
	protected void addShadow(final String name, final BoaType t) {
		names.put(name, members.size());
		members.add(t);
	}

	/**
	 * Looks up an attribute name and returns the replacement AST for that expression.
	 *
	 * @param name the name of the attribute to look up
	 * @param nodeId the identifier token of the node we are trying to select on
	 * @param env the current SymbolTable environment
	 * @return a replacement AST for the attribute selector
	 */
	public abstract Node lookupCodegen(final String name, final String nodeId, final SymbolTable env);

	/**
	 * Returns an {@link boa.compiler.ast.expressions.Expression} representing
	 * the Kind of the shadow type.
	 *
	 * @param env the current SymbolTable environment
	 * @return an Expression to select a specific Kind for the shadow
	 */
	public abstract Expression getKindExpression(final SymbolTable env);

	/**
	 * Returns an {@link boa.compiler.ast.expressions.Expression} representing
	 * the Kind of the shadow type.
	 *
	 * @param kind the enum of kinds
	 * @param attr the attribute to select the specific kind
	 * @param t the compiler type for the kind
	 * @param env the current SymbolTable environment
	 * @return an Expression to select a specific Kind for the shadow
	 */
	protected Expression getKindExpression(final String kind, final String attr, final BoaProtoMap t, final SymbolTable env) {
		final Selector s = new Selector(ASTFactory.createIdentifier(attr, env));
		final Factor f = new Factor(ASTFactory.createIdentifier(kind, env)).addOp(s);
		final Expression tree = ASTFactory.createFactorExpr(f);

		s.env = f.env = env;

		s.type = f.type = f.getOperand().type = tree.type = t;

		return tree;
	}
}
