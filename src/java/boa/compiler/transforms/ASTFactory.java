/*
 * Copyright 2016, Hridesh Rajan, Robert Dyer
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

import boa.compiler.ast.types.AbstractType;
import boa.compiler.ast.Call;
import boa.compiler.ast.Comparison;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Operand;
import boa.compiler.ast.statements.ExprStatement;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.Term;
import boa.compiler.SymbolTable;
import boa.types.BoaType;

/**
 * A helper class for creating new AST trees.
 *
 * @author rdyer
 */
public class ASTFactory {
	public static VarDeclStatement createVarDecl(final String name, final AbstractType type, final BoaType t, final SymbolTable env) {
		final VarDeclStatement var = new VarDeclStatement(
				ASTFactory.createIdentifier(name, env),
				type
			);
		var.type = t;
		return var;
	}

	public static VarDeclStatement createVarDecl(final String name, final Operand init, final BoaType t, final SymbolTable env) {
		final VarDeclStatement var = new VarDeclStatement(
				new Identifier(name),
				new Expression(
					new Conjunction(
						new Comparison(
							new SimpleExpr(
								new Term(
									new Factor(init)
								)
							)
						)
					)
				)
			);
		var.type = var.getInitializer().type = t;
		var.env = env;
		return var;
	}

	public static Identifier createIdentifier(final String name, final SymbolTable env) {
		final Identifier id = new Identifier(name);
		id.env = env;
		return id;
	}

	public static Expression createIdentifierExpr(final String name, final SymbolTable env, BoaType t) {
		final Factor f = new Factor(ASTFactory.createIdentifier(name, env));
		f.env = env;

		final Expression exp = new Expression(
			new Conjunction(
				new Comparison(
					new SimpleExpr(
						new Term(f)
					)
				)
			)
		);
		exp.type = t;
		return exp;
	}

	public static ExprStatement createCall(final String name, final SymbolTable env, final BoaType retType, final Expression... args) {
		final Expression exp = ASTFactory.createIdentifierExpr(name, env, retType);

		final Call c = new Call();
		for (final Expression e : args)
			c.addArg(e);
		c.env = env;

		exp.getLhs().getLhs().getLhs().getLhs().getLhs().addOp(c);

		return new ExprStatement(exp);
	}

	public static Expression createFactorExpr(final Operand op) {
		return new Expression(
			new Conjunction(
				new Comparison(
					new SimpleExpr(
						new Term(
							new Factor(op)
						)
					)
				)
			)
		);
	}

	public static Expression createComparison(final Operand lhs, final String op, final Operand rhs) {
		return new Expression(
			new Conjunction(
				new Comparison(
					new SimpleExpr(
						new Term(
							new Factor(lhs)
						)
					),
					op,
					new SimpleExpr(
						new Term(
							new Factor(rhs)
						)
					)
				)
			)
		);
	}
}
