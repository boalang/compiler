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

import boa.compiler.ast.Call;
import boa.compiler.ast.Comparison;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Index;
import boa.compiler.ast.literals.IntegerLiteral;
import boa.compiler.ast.literals.StringLiteral;
import boa.compiler.ast.Operand;
import boa.compiler.ast.Selector;
import boa.compiler.ast.statements.ExprStatement;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.Term;
import boa.compiler.ast.types.AbstractType;
import boa.compiler.SymbolTable;
import boa.types.BoaInt;
import boa.types.BoaString;
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
				ASTFactory.createFactorExpr(init)
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

		final Expression exp = ASTFactory.createFactorExpr(f);
		exp.type = t;
		return exp;
	}

	public static Expression createIntLiteral(final long value) {
		final Expression exp = ASTFactory.createFactorExpr(new IntegerLiteral("" + value));
		exp.type = new BoaInt();
		return exp;
	}

	public static Expression createStringLiteral(final String value) {
		final Expression exp = ASTFactory.createFactorExpr(new StringLiteral(value));
		exp.type = new BoaString();
		return exp;
	}

	public static Expression createCallExpr(final String name, final SymbolTable env, final BoaType retType, final Expression... args) {
		final Expression exp = ASTFactory.createIdentifierExpr(name, env, retType);

		final Call c = new Call();
		for (final Expression e : args)
			c.addArg(e);
		c.env = env;

		exp.getLhs().getLhs().getLhs().getLhs().getLhs().addOp(c);

		return exp;
	}

	public static ExprStatement createCall(final String name, final SymbolTable env, final BoaType retType, final Expression... args) {
		return new ExprStatement(ASTFactory.createCallExpr(name, env, retType, args));
	}

	public static Expression createFactorExpr(final Operand op) {
		return ASTFactory.createFactorExpr(new Factor(op));
	}

	public static Expression createFactorExpr(final Factor f) {
		return new Expression(
			new Conjunction(
				new Comparison(
					new SimpleExpr(
						new Term(f)
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

	public static Expression createSelector(final Identifier id, final String selector, final BoaType selectorType, final BoaType factorType, final SymbolTable env) {
		final Selector s = new Selector(ASTFactory.createIdentifier(selector, env));
		final Factor f = new Factor(id).addOp(s);
		final Expression tree = ASTFactory.createFactorExpr(f);

		s.env = f.env = env;

		s.type = selectorType;
		f.type = tree.type = factorType;

		return tree;
	}

	public static Index createIndex(final Expression idx, final SymbolTable env) {
		final Index i = new Index(idx);
		i.env = env;
		return i;
	}

	public static Factor getFactorFromExp(final Expression exp) {
		return exp.getLhs().getLhs().getLhs().getLhs().getLhs();
	}
}
