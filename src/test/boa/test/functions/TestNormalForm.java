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
import static boa.functions.BoaNormalFormIntrinsics.parseexpression;

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
	private static Expression exp1 = parseexpression("((a < b || !b) || (a < b && a) || !b || !a) && a < b");
	@Test
	public void testExp1simple() {
		assertEquals(parseexpression("a < b"), BoaNormalFormIntrinsics.simplify(exp1));
	}
	@Test
	public void testExp1nnf() {
		assertEquals(parseexpression("a < b"), BoaNormalFormIntrinsics.NNF(exp1));
	}
	@Test
	public void testExp1cnf() {
		assertEquals(parseexpression("a < b"), BoaNormalFormIntrinsics.CNF(exp1));
	}
	@Test
	public void testExp1dnf() {
		assertEquals(parseexpression("a < b"), BoaNormalFormIntrinsics.DNF(exp1));
	}

    ///////////////////

	private static Expression exp2 = parseexpression("(!a || b) && a");
	@Test
	public void testExp2simple() {
		assertEquals(parseexpression("a && b"), BoaNormalFormIntrinsics.simplify(exp2));
	}
	@Test
	public void testExp2nnf() {
		assertEquals(parseexpression("a && b"), BoaNormalFormIntrinsics.NNF(exp2));
	}
	@Test
	public void testExp2cnf() {
		assertEquals(parseexpression("a && b"), BoaNormalFormIntrinsics.CNF(exp2));
	}
	@Test
	public void testExp2dnf() {
		assertEquals(parseexpression("a && b"), BoaNormalFormIntrinsics.DNF(exp2));
	}

    ///////////////////

	private static Expression exp3 = parseexpression("!(!a)");
	@Test
	public void testExp3simple() {
		assertEquals(exp3, BoaNormalFormIntrinsics.simplify(exp3));
	}
	@Test
	public void testExp3nnf() {
		assertEquals(parseexpression("a"), BoaNormalFormIntrinsics.NNF(exp3));
	}
	@Test
	public void testExp3cnf() {
		assertEquals(parseexpression("a"), BoaNormalFormIntrinsics.CNF(exp3));
	}
	@Test
	public void testExp3dnf() {
		assertEquals(parseexpression("a"), BoaNormalFormIntrinsics.DNF(exp3));
	}

    ///////////////////

	private static Expression exp4 = parseexpression("(!a && a) || a");
	@Test
	public void testExp4simple() {
		assertEquals(parseexpression("a"), BoaNormalFormIntrinsics.simplify(exp4));
	}
	@Test
	public void testExp4nnf() {
		assertEquals(parseexpression("a"), BoaNormalFormIntrinsics.NNF(exp4));
	}
	@Test
	public void testExp4cnf() {
		assertEquals(parseexpression("a"), BoaNormalFormIntrinsics.CNF(exp4));
	}
	@Test
	public void testExp4dnf() {
		assertEquals(parseexpression("a"), BoaNormalFormIntrinsics.DNF(exp4));
	}

    ///////////////////

	private static Expression exp5 = parseexpression("a");
	@Test
	public void testExp5simple() {
		assertEquals(parseexpression("a"), BoaNormalFormIntrinsics.simplify(exp5));
	}
	@Test
	public void testExp5nnf() {
		assertEquals(parseexpression("a"), BoaNormalFormIntrinsics.NNF(exp5));
	}
	@Test
	public void testExp5cnf() {
		assertEquals(parseexpression("a"), BoaNormalFormIntrinsics.CNF(exp5));
	}
	@Test
	public void testExp5dnf() {
		assertEquals(parseexpression("a"), BoaNormalFormIntrinsics.DNF(exp5));
	}

    ///////////////////

	private static Expression exp6 = parseexpression("(!a && b) || a");
	@Test
	public void testExp6simple() {
		assertEquals(parseexpression("a || b"), BoaNormalFormIntrinsics.simplify(exp6));
	}
	@Test
	public void testExp6nnf() {
		assertEquals(parseexpression("a || b"), BoaNormalFormIntrinsics.NNF(exp6));
	}
	@Test
	public void testExp6cnf() {
		assertEquals(parseexpression("a || b"), BoaNormalFormIntrinsics.CNF(exp6));
	}
	@Test
	public void testExp6dnf() {
		assertEquals(parseexpression("a || b"), BoaNormalFormIntrinsics.DNF(exp6));
	}

    ///////////////////

	private static Expression exp7 = parseexpression("(a < b && b) && a");
	@Test
	public void testExp7simple() {
		assertEquals(BoaNormalFormIntrinsics.simplify(parseexpression("a && a < b && b")), BoaNormalFormIntrinsics.simplify(exp7));
	}
	@Test
	public void testExp7nnf() {
		assertEquals(BoaNormalFormIntrinsics.simplify(parseexpression("a && a < b && b")), BoaNormalFormIntrinsics.NNF(exp7));
	}
	@Test
	public void testExp7cnf() {
		assertEquals(BoaNormalFormIntrinsics.simplify(parseexpression("a && a < b && b")), BoaNormalFormIntrinsics.CNF(exp7));
	}
	@Test
	public void testExp7dnf() {
		assertEquals(BoaNormalFormIntrinsics.simplify(parseexpression("a && a < b && b")), BoaNormalFormIntrinsics.DNF(exp7));
	}

    ///////////////////

	private static Expression exp8 = parseexpression("(!(a < b) || b) && a");
	@Test
	public void testExp8simple() {
		assertEquals(exp8, BoaNormalFormIntrinsics.simplify(exp8));
	}
	@Test
	public void testExp8nnf() {
		assertEquals(parseexpression("(a >= b || b) && a"), BoaNormalFormIntrinsics.NNF(exp8));
	}
	@Test
	public void testExp8cnf() {
		assertEquals(parseexpression("(a >= b || b) && a"), BoaNormalFormIntrinsics.CNF(exp8));
	}
	@Test
	public void testExp8dnf() {
		assertEquals(parseexpression("(a && a >= b) || (a && b)"), BoaNormalFormIntrinsics.DNF(exp8));
	}

    ///////////////////

	private static Expression exp9 = parseexpression("!(!(a < b) || b)");
	@Test
	public void testExp9simple() {
		assertEquals(exp9, BoaNormalFormIntrinsics.simplify(exp9));
	}
	@Test
	public void testExp9nnf() {
		assertEquals(parseexpression("!b && a < b"), BoaNormalFormIntrinsics.NNF(exp9));
	}
	@Test
	public void testExp9cnf() {
		assertEquals(parseexpression("!b && a < b"), BoaNormalFormIntrinsics.CNF(exp9));
	}
	@Test
	public void testExp9dnf() {
		assertEquals(parseexpression("!b && a < b"), BoaNormalFormIntrinsics.DNF(exp9));
	}

    ///////////////////

	private static Expression exp10 = parseexpression("(!(a < b) && b) || a");
	@Test
	public void testExp10simple() {
		assertEquals(exp10, BoaNormalFormIntrinsics.simplify(exp10));
	}
	@Test
	public void testExp10nnf() {
		assertEquals(parseexpression("(a >= b && b) || a"), BoaNormalFormIntrinsics.NNF(exp10));
	}
	@Test
	public void testExp10cnf() {
		assertEquals(parseexpression("(a || a >= b) && (a || b)"), BoaNormalFormIntrinsics.CNF(exp10));
	}
	@Test
	public void testExp10dnf() {
		assertEquals(parseexpression("(a >= b && b) || a"), BoaNormalFormIntrinsics.DNF(exp10));
	}

    ///////////////////

	private static Expression exp11 = parseexpression("!(a < b) || b");
	@Test
	public void testExp11simple() {
		assertEquals(exp11, BoaNormalFormIntrinsics.simplify(exp11));
	}
	@Test
	public void testExp11nnf() {
		assertEquals(parseexpression("a >= b || b"), BoaNormalFormIntrinsics.NNF(exp11));
	}
	@Test
	public void testExp11cnf() {
		assertEquals(parseexpression("a >= b || b"), BoaNormalFormIntrinsics.CNF(exp11));
	}
	@Test
	public void testExp11dnf() {
		assertEquals(parseexpression("a >= b || b"), BoaNormalFormIntrinsics.DNF(exp11));
	}
}
