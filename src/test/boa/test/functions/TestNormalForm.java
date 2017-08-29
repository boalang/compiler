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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import boa.functions.BoaNormalFormIntrinsics;
import boa.types.Ast.Expression;

/**
 * Test the expression simplifying and normal form functions (NNF/CNF/DNF).
 *
 * @author rdyer
 */
@RunWith(Parameterized.class)
public class TestNormalForm {
	@Parameters(name = "{index}][{0}")
	public static Collection<String[]> expressions() {
		return Arrays.asList(new String[][] {
			{ "((a < b || !b) || (a < b && a) || !b || !a) && a < b", "a < b", "a < b", "a < b", "a < b" },
			{ "(!a || b) && a", "a && b", "a && b", "a && b", "a && b" },
			{ "!(!a)", "!(!a)", "a", "a", "a" },
			{ "(!a && a) || a", "a", "a", "a", "a" },
			{ "a", "a", "a", "a", "a" },
			{ "(!a && b) || a", "a || b", "a || b", "a || b", "a || b" },
			// FIXME seems the JDT parser only puts 2 operands per operator?
			//{ "(a < b && b) && a", "a && a < b && b", "a && a < b && b", "a && a < b && b", "a && a < b && b" },
			{ "(!(a < b) || b) && a", "(!(a < b) || b) && a", "(a >= b || b) && a", "(a >= b || b) && a", "(a && a >= b) || (a && b)" },
			{ "!(!(a < b) || b)", "!(!(a < b) || b)", "!b && a < b", "!b && a < b", "!b && a < b" },
			{ "(!(a < b) && b) || a", "(!(a < b) && b) || a", "(a >= b && b) || a", "(a || a >= b) && (a || b)", "(a >= b && b) || a" },
			{ "!(a < b) || b", "!(a < b) || b", "a >= b || b", "a >= b || b", "a >= b || b" }
		});
	}

	private Expression e = null;
	private Expression simple = null;
	private Expression nnf = null;
	private Expression cnf = null;
	private Expression dnf = null;

	public TestNormalForm(final String e, final String simple, final String nnf, final String cnf, final String dnf) {
		this.e = parseexpression(e);
		this.simple = parseexpression(simple);
		this.nnf = parseexpression(nnf);
		this.cnf = parseexpression(cnf);
		this.dnf = parseexpression(dnf);
	}

	@Test
	public void testSimplify() throws Exception {
		assertEquals(simple, BoaNormalFormIntrinsics.simplify(e));
	}

	@Test
	public void testNNF() throws Exception {
		assertEquals(nnf, BoaNormalFormIntrinsics.NNF(e));
	}

	@Test
	public void testCNF() throws Exception {
		assertEquals(cnf, BoaNormalFormIntrinsics.CNF(e));
	}

	@Test
	public void testDNF() throws Exception {
		assertEquals(dnf, BoaNormalFormIntrinsics.DNF(e));
	}
}
