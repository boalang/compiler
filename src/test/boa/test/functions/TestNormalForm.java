/*
 * Copyright 2017, Robert Dyer, 
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
package boa.test.functions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import boa.functions.BoaNormalFormIntrinsics;
import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;

/**
 * @author rdyer
 */
@RunWith(JUnit4.class)
public class TestNormalForm {
	private static Expression var(final String var) {
		final Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.VARACCESS);
		b.setVariable(var);
		return b.build();
	}

	private static Expression paren(final Expression e) {
		final Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.PAREN);
		b.addExpressions(e);
		return b.build();
	}

	private static Expression not(final Expression e) {
		final Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.LOGICAL_NOT);
		b.addExpressions(e);
		return b.build();
	}

	private static Expression and(final Expression... exps) {
		final Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.LOGICAL_AND);
		for (final Expression e : exps)
			b.addExpressions(e);
		return b.build();
	}

	private static Expression or(final Expression... exps) {
		final Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.LOGICAL_OR);
		for (final Expression e : exps)
			b.addExpressions(e);
		return b.build();
	}

	private static Expression lt(final Expression lhs, final Expression rhs) {
		final Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.LT);
		b.addExpressions(lhs);
		b.addExpressions(rhs);
		return b.build();
	}

	private static Expression gte(final Expression lhs, final Expression rhs) {
		final Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.GTEQ);
		b.addExpressions(lhs);
		b.addExpressions(rhs);
		return b.build();
	}

	// ((a < b || !b) || (a < b && a) || !b || !a) && a < b
	private static Expression exp1 =
		and(
			or(
				or(
					or(
						or(
							lt(var("a"), var("b")),
							not(var("b"))
						),
						and(
							lt(var("a"), var("b")),
							var("a")
						)
					),
					not(var("b"))
				),
				not(var("a"))
			),
			lt(var("a"), var("b"))
		);
	@Test
	public void testExp1simple() {
		assertEquals(lt(var("a"), var("b")), BoaNormalFormIntrinsics.simplify(exp1));
	}
	@Test
	public void testExp1nnf() {
		assertEquals(lt(var("a"), var("b")), BoaNormalFormIntrinsics.NNF(exp1));
	}
	@Test
	public void testExp1cnf() {
		assertEquals(lt(var("a"), var("b")), BoaNormalFormIntrinsics.CNF(exp1));
	}
	@Test
	public void testExp1dnf() {
		assertEquals(lt(var("a"), var("b")), BoaNormalFormIntrinsics.DNF(exp1));
	}

	// (!a || b) && a
	private static Expression exp2 =
		and(
			or(
				not(var("a")),
				var("b")
			),
			var("a")
		);
	@Test
	public void testExp2simple() {
		assertEquals(and(var("a"), var("b")), BoaNormalFormIntrinsics.simplify(exp2));
	}
	@Test
	public void testExp2nnf() {
		assertEquals(and(var("a"), var("b")), BoaNormalFormIntrinsics.NNF(exp2));
	}
	@Test
	public void testExp2cnf() {
		assertEquals(and(var("a"), var("b")), BoaNormalFormIntrinsics.CNF(exp2));
	}
	@Test
	public void testExp2dnf() {
		assertEquals(and(var("a"), var("b")), BoaNormalFormIntrinsics.DNF(exp2));
	}

	// !(!a)
	private static Expression exp3 = not(paren(not(var("a"))));
	@Test
	public void testExp3simple() {
		assertEquals(exp3, BoaNormalFormIntrinsics.simplify(exp3));
	}
	@Test
	public void testExp3nnf() {
		assertEquals(var("a"), BoaNormalFormIntrinsics.NNF(exp3));
	}
	@Test
	public void testExp3cnf() {
		assertEquals(var("a"), BoaNormalFormIntrinsics.CNF(exp3));
	}
	@Test
	public void testExp3dnf() {
		assertEquals(var("a"), BoaNormalFormIntrinsics.DNF(exp3));
	}

	// (!a && a) || a
	private static Expression exp4 =
		or(
			and(
				not(var("a")),
				var("a")
			),
			var("a")
		);
	@Test
	public void testExp4simple() {
		assertEquals(var("a"), BoaNormalFormIntrinsics.simplify(exp4));
	}
	@Test
	public void testExp4nnf() {
		assertEquals(var("a"), BoaNormalFormIntrinsics.NNF(exp4));
	}
	@Test
	public void testExp4cnf() {
		assertEquals(var("a"), BoaNormalFormIntrinsics.CNF(exp4));
	}
	@Test
	public void testExp4dnf() {
		assertEquals(var("a"), BoaNormalFormIntrinsics.DNF(exp4));
	}

	// a
	private static Expression exp5 = var("a");
	@Test
	public void testExp5simple() {
		assertEquals(var("a"), BoaNormalFormIntrinsics.simplify(exp5));
	}
	@Test
	public void testExp5nnf() {
		assertEquals(var("a"), BoaNormalFormIntrinsics.NNF(exp5));
	}
	@Test
	public void testExp5cnf() {
		assertEquals(var("a"), BoaNormalFormIntrinsics.CNF(exp5));
	}
	@Test
	public void testExp5dnf() {
		assertEquals(var("a"), BoaNormalFormIntrinsics.DNF(exp5));
	}

	// (!a && b) || a
	private static Expression exp6 =
		or(
			and(
				not(var("a")),
				var("b")
			),
			var("a")
		);
	@Test
	public void testExp6simple() {
		assertEquals(or(var("a"), var("b")), BoaNormalFormIntrinsics.simplify(exp6));
	}
	@Test
	public void testExp6nnf() {
		assertEquals(or(var("a"), var("b")), BoaNormalFormIntrinsics.NNF(exp6));
	}
	@Test
	public void testExp6cnf() {
		assertEquals(or(var("a"), var("b")), BoaNormalFormIntrinsics.CNF(exp6));
	}
	@Test
	public void testExp6dnf() {
		assertEquals(or(var("a"), var("b")), BoaNormalFormIntrinsics.DNF(exp6));
	}

	// (a < b && b) && a
	private static Expression exp7 =
		and(
			and(
				lt(var("a"), var("b")),
				var("b")
			),
			var("a")
		);
	@Test
	public void testExp7simple() {
		assertEquals(and(var("a"), lt(var("a"), var("b")), var("b")), BoaNormalFormIntrinsics.simplify(exp7));
	}
	@Test
	public void testExp7nnf() {
		assertEquals(and(var("a"), lt(var("a"), var("b")), var("b")), BoaNormalFormIntrinsics.NNF(exp7));
	}
	@Test
	public void testExp7cnf() {
		assertEquals(and(var("a"), lt(var("a"), var("b")), var("b")), BoaNormalFormIntrinsics.CNF(exp7));
	}
	@Test
	public void testExp7dnf() {
		assertEquals(and(var("a"), lt(var("a"), var("b")), var("b")), BoaNormalFormIntrinsics.DNF(exp7));
	}

	// (!(a < b) || b) && a
	private static Expression exp8 =
		and(
			or(
				not(
					lt(var("a"), var("b"))
				),
				var("b")
			),
			var("a")
		);
	@Test
	public void testExp8simple() {
		assertEquals(exp8, BoaNormalFormIntrinsics.simplify(exp8));
	}
	@Test
	public void testExp8nnf() {
		assertEquals(and(or(gte(var("a"), var("b")), var("b")), var("a")), BoaNormalFormIntrinsics.NNF(exp8));
	}
	@Test
	public void testExp8cnf() {
		assertEquals(and(or(gte(var("a"), var("b")), var("b")), var("a")), BoaNormalFormIntrinsics.CNF(exp8));
	}
	@Test
	public void testExp8dnf() {
		assertEquals(or(and(var("a"), gte(var("a"), var("b"))), and(var("a"), var("b"))), BoaNormalFormIntrinsics.DNF(exp8));
	}

	// !(!(a < b) || b)
	private static Expression exp9 =
		not(
			or(
				not(
					lt(var("a"), var("b"))
				),
				var("b")
			)
		);
	@Test
	public void testExp9simple() {
		assertEquals(exp9, BoaNormalFormIntrinsics.simplify(exp9));
	}
	@Test
	public void testExp9nnf() {
		assertEquals(and(not(var("b")), lt(var("a"), var("b"))), BoaNormalFormIntrinsics.NNF(exp9));
	}
	@Test
	public void testExp9cnf() {
		assertEquals(and(not(var("b")), lt(var("a"), var("b"))), BoaNormalFormIntrinsics.CNF(exp9));
	}
	@Test
	public void testExp9dnf() {
		assertEquals(and(not(var("b")), lt(var("a"), var("b"))), BoaNormalFormIntrinsics.DNF(exp9));
	}

	// (!(a < b) && b) || a
	private static Expression exp10 =
		or(
			and(
				not(
					lt(var("a"), var("b"))
				),
				var("b")
			),
			var("a")
		);
	@Test
	public void testExp10simple() {
		assertEquals(exp10, BoaNormalFormIntrinsics.simplify(exp10));
	}
	@Test
	public void testExp10nnf() {
		assertEquals(or(and(gte(var("a"), var("b")), var("b")), var("a")), BoaNormalFormIntrinsics.NNF(exp10));
	}
	@Test
	public void testExp10cnf() {
		assertEquals(and(or(var("a"), gte(var("a"), var("b"))), or(var("a"), var("b"))), BoaNormalFormIntrinsics.CNF(exp10));
	}
	@Test
	public void testExp10dnf() {
		assertEquals(or(and(gte(var("a"), var("b")), var("b")), var("a")), BoaNormalFormIntrinsics.DNF(exp10));
	}

	// !(a < b) || b
	private static Expression exp11 =
		or(
			not(
				lt(var("a"), var("b"))
			),
			var("b")
		);
	@Test
	public void testExp11simple() {
		assertEquals(exp11, BoaNormalFormIntrinsics.simplify(exp11));
	}
	@Test
	public void testExp11nnf() {
		assertEquals(or(gte(var("a"), var("b")), var("b")), BoaNormalFormIntrinsics.NNF(exp11));
	}
	@Test
	public void testExp11cnf() {
		assertEquals(or(gte(var("a"), var("b")), var("b")), BoaNormalFormIntrinsics.CNF(exp11));
	}
	@Test
	public void testExp11dnf() {
		assertEquals(or(gte(var("a"), var("b")), var("b")), BoaNormalFormIntrinsics.DNF(exp11));
	}
}
