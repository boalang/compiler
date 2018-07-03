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
import static boa.functions.BoaAstIntrinsics.parseexpression;

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
			{ "!(!a)", "a", "a", "a", "a" },
			{ "!(!a || a)", "false", "false", "false", "false" },
			{ "!(!a && a)", "true", "true", "true", "true" },
			{ "!(!a && !a)", "a", "a", "a", "a" },
			{ "(!a && a) || a", "a", "a", "a", "a" },
			{ "a", "a", "a", "a", "a" },
			{ "(!a && b) || a", "a || b", "a || b", "a || b", "a || b" },
			{ "(a < b && b) && a", "a && a < b && b", "a && a < b && b", "a && a < b && b", "a && a < b && b" },
			{ "(!(a < b) || b) && a", "(!(a < b) || b) && a", "(a >= b || b) && a", "(a >= b || b) && a", "(a && a >= b) || (a && b)" },
			{ "!(!(a < b) || b)", "!(!(a < b) || b)", "!b && a < b", "!b && a < b", "!b && a < b" },
			{ "(!(a < b) && b) || a", "!(a < b) && b || a", "a >= b && b || a", "(a || a >= b) && (a || b)", "a >= b && b || a" },
			{ "!(a < b) || b", "!(a < b) || b", "a >= b || b", "a >= b || b", "a >= b || b" },

			{ "(a)", "a", "a", "a", "a"},
			{ "!(a)", "!a", "!a", "!a", "!a"},
			{ "(!a)", "!a", "!a", "!a", "!a"},

			{ "a && a", "a", "a", "a", "a"},
			{ "a || a", "a", "a", "a", "a"},
			{ "(a && a)", "a", "a", "a", "a"},
			{ "(a || a)", "a", "a", "a", "a"},

			{ "!a && a", "false", "false", "false", "false"},
			{ "a && !a", "false", "false", "false", "false"},
			{ "a || !a", "true", "true", "true", "true"},
			{ "!a || a", "true", "true", "true", "true"},

			{ "(!a && a)", "false", "false", "false", "false"},
			{ "(a && !a)", "false", "false", "false", "false"},
			{ "!(!a && !a)", "a", "a", "a", "a"},
			{ "(a || !a)", "true", "true", "true", "true"},
			{ "(!a || a)", "true", "true", "true", "true"},
			{ "!(!a || !a)", "a", "a", "a", "a"},

			{ "a && b", "a && b", "a && b", "a && b", "a && b"},
			{ "a || b", "a || b", "a || b", "a || b", "a || b"},
			{ "!a && b", "!a && b", "!a && b", "!a && b", "!a && b"},
			{ "a && !b", "!b && a", "!b && a", "!b && a", "!b && a"},
			{ "!a && !b", "!a && !b", "!a && !b", "!a && !b", "!a && !b"},
			{ "!a || b", "!a || b", "!a || b", "!a || b", "!a || b"},
			{ "a || !b", "!b || a", "!b || a", "!b || a", "!b || a"},
			{ "!a || !b", "!a || !b", "!a || !b", "!a || !b", "!a || !b"},

			{ "(a && b)", "a && b", "a && b", "a && b", "a && b"},
			{ "!(a && b)", "!(a && b)", "!a || !b", "!a || !b", "!a || !b"},
			{ "!(!a && b)", "!(!a && b)", "!b || a", "!b || a", "!b || a"},
			{ "!(a && !b)", "!(!b && a)", "!a || b", "!a || b", "!a || b"},
			{ "!(!a && !b)", "!(!a && !b)", "a || b", "a || b", "a || b"},
			{ "(a || b)", "a || b", "a || b", "a || b", "a || b"},
			{ "!(a || b)", "!(a || b)", "!a && !b", "!a && !b", "!a && !b"},
			{ "!(!a || b)", "!(!a || b)", "!b && a", "!b && a", "!b && a"},
			{ "!(!a || !b)", "!(!a || !b)", "a && b", "a && b", "a && b"},

			// FIX JDT PARSING

			// { "a || a || a", "a", "a", "a", "a"},
			// { "(a || a || a)", "a", "a", "a", "a"},
			// { "a && a && a", "a", "a", "a", "a"},
			// { "(a && a && a)", "a", "a", "a", "a"},

			// { "!a || a || a", "true", "true", "true", "true"},
			// { "(!a || a || a)", "true", "true", "true", "true"},
			// { "a || !a || a", "true", "true", "true", "true"},
			// { "(a || !a || a)", "true", "true", "true", "true"},
			// { "a || a || !a", "true", "true", "true", "true"},
			// { "(a || a || !a)", "true", "true", "true", "true"},
			// { "!a && a && a", "false", "false", "false", "false"},
			// { "(!a && a && a)", "false", "false", "false", "false"},
			// { "a && !a && a", "false", "false", "false", "false"},
			// { "(a && !a && a)", "false", "false", "false", "false"},
			// { "a && a && !a", "false", "false", "false", "false"},
			// { "(a && a && !a)", "false", "false", "false", "false"},

			// { "!(!a || a || a)", "false", "false", "false", "false"},
			// { "!(a || !a || a)", "false", "false", "false", "false"},
			// { "!(a || a || !a)", "false", "false", "false", "false"},
			// { "!(!a && a && a)", "true", "true", "true", "true"},
			// { "!(a && !a && a)", "true", "true", "true", "true"},
			// { "!(a && a && !a)", "true", "true", "true", "true"},

			{ "a || a && a", "a", "a", "a", "a"},
			{ "(a || a) && a", "a", "a", "a", "a"},
			{ "a || (a && a)", "a", "a", "a", "a"},
			{ "(a || a && a)", "a", "a", "a", "a"},
			{ "a && a || a", "a", "a", "a", "a"},
			{ "(a && a) || a", "a", "a", "a", "a"},
			{ "a && (a || a)", "a", "a", "a", "a"},
			{ "(a && a || a)", "a", "a", "a", "a"},

			{ "!a || a && a", "true", "true", "true", "true"},
			{ "(!a || a) && a", "a", "a", "a", "a"},
			{ "!a || (a && a)", "true", "true", "true", "true"},
			{ "(!a || a && a)", "true", "true", "true", "true"},
			{ "a || !a && a", "a", "a", "a", "a"},
			{ "(a || !a) && a", "a", "a", "a", "a"},
			{ "a || (!a && a)", "a", "a", "a", "a"},
			{ "(a || !a && a)", "a", "a", "a", "a"},
			{ "a || a && !a", "a", "a", "a", "a"},
			{ "(a || a) && !a", "false", "false", "false", "false"},
			{ "a || (a && !a)", "a", "a", "a", "a"},
			{ "(a || a && !a)", "a", "a", "a", "a"},
			{ "!a && a || a", "a", "a", "a", "a"},
			{ "(!a && a) || a", "a", "a", "a", "a"},
			{ "!a && (a || a)", "false", "false", "false", "false"},
			{ "(!a && a || a)", "a", "a", "a", "a"},
			{ "a && !a || a", "a", "a", "a", "a"},
			{ "(a && !a) || a", "a", "a", "a", "a"},
			{ "a && (!a || a)", "a", "a", "a", "a"},
			{ "(a && !a || a)", "a", "a", "a", "a"},
			{ "a && a || !a", "true", "true", "true", "true"},
			{ "(a && a) || !a", "true", "true", "true", "true"},
			{ "a && (a || !a)", "a", "a", "a", "a"},
			{ "(a && a || !a)", "true", "true", "true", "true"},

			{ "!a || !a && a", "!a", "!a", "!a", "!a"},
			{ "!a || (!a && a)", "!a", "!a", "!a", "!a"},
			{ "!a || !(!a && a)", "true", "true", "true", "true"},
			{ "!(!a || !(!a && a))", "false", "false", "false", "false"},
			{ "a || !a && !a", "true", "true", "true", "true"},
			{ "a || (!a && !a)", "true", "true", "true", "true"},
			{ "a || !(!a && !a)", "a", "a", "a", "a"},
			{ "!(a || !(!a && !a))", "!a", "!a", "!a", "!a"},
			{ "!a || a && !a", "!a", "!a", "!a", "!a"},
			{ "!a || (a && !a)", "!a", "!a", "!a", "!a"},
			{ "!a || !(a && !a)", "true", "true", "true", "true"},
			{ "!(!a || !(a && !a))", "false", "false", "false", "false"},
			{ "!a && !a || a", "true", "true", "true", "true"},
			{ "!a && (!a || a)", "!a", "!a", "!a", "!a"},
			{ "!a && !(!a || a)", "false", "false", "false", "false"},
			{ "!(!a && !(!a || a))", "true", "true", "true", "true"},
			{ "a && !a || !a", "!a", "!a", "!a", "!a"},
			{ "a && (!a || !a)", "false", "false", "false", "false"},
			{ "a && !(!a || !a)", "a", "a", "a", "a"},
			{ "!(a && !(!a || !a))", "!a", "!a", "!a", "!a"},
			{ "!a && a || !a", "!a", "!a", "!a", "!a"},
			{ "!a && (a || !a)", "!a", "!a", "!a", "!a"},
			{ "!a && !(a || !a)", "false", "false", "false", "false"},
			{ "!(!a && !(a || !a))", "true", "true", "true", "true"},

			{ "!a || !a && !a", "!a", "!a", "!a", "!a"},
			{ "!a || !(!a && !a)", "true", "true", "true", "true"},
			{ "!(!a || !(!a && !a))", "false", "false", "false", "false"},
			{ "!a && !a || !a", "!a", "!a", "!a", "!a"},
			{ "!a && !(!a || !a)", "false", "false", "false", "false"},
			{ "!(!a && !(!a || !a))", "true", "true", "true", "true"},

			// FIX JDT PARSING

			// { "a || b || a", "a || b", "a || b", "a || b", "a || b"},
			// { "a || a || b", "a || b", "a || b", "a || b", "a || b"},
			// { "a || b || b", "a || b", "a || b", "a || b", "a || b"},
			// { "a && b && a", "a && b", "a && b", "a && b", "a && b"},
			// { "a && a && b", "a && b", "a && b", "a && b", "a && b"},
			// { "a && b && b", "a && b", "a && b", "a && b", "a && b"},

			// { "a || !b || a", "!b || a", "!b || a", "!b || a", "!b || a"},
			// { "a || b || !a", "true", "true", "true", "true"},
			// { "a || !a || b", "true", "true", "true", "true"},
			// { "a || a || !b", "!b || a", "!b || a", "!b || a", "!b || a"},
			// { "a || !b || b", "true", "true", "true", "true"},
			// { "a || !b || !b", "!b || a", "!b || a", "!b || a", "!b || a"},
			// { "a && !b && a", "!b && a", "!b && a", "!b && a", "!b && a"},
			// { "a && b && !a", "false", "false", "false", "false"},
			// { "a && !a && b", "false", "false", "false", "false"},
			// { "a && a && !b", "!b && a", "!b && a", "!b && a", "!b && a"},
			// { "a && !b && b", "false", "false", "false", "false"},
			// { "a && !b && !b", "!b && a", "!b && a", "!b && a", "!b && a"},

			// { "a || !(!b || a)", "a || !(!b || a)", "a || b", "a || b", "a || b"},
			// { "!(a || !(!b || a))", "!(a || !(!b || a))", "!a && !b", "!a && !b", "!a && !b"},
			// { "a || !(b || !a)", "a || !(b || !a)", "!b || a", "!b || a", "!b || a"},
			// { "!(a || !(b || !a))", "!(a || !(b || !a))", "!a && b", "!a && b", "!a && b"},

			{ "a || b && a", "a", "a", "a", "a"},
			{ "a || a && b", "a", "a", "a", "a"},
			{ "a || b && b", "a || b", "a || b", "a || b", "a || b"},
			{ "a && b || a", "a", "a", "a", "a"},
			{ "a && a || b", "a || b", "a || b", "a || b", "a || b"},
			{ "a && b || b", "b", "b", "b", "b"},
			{ "(a || b) && a", "a", "a", "a", "a"},
			{ "(a || a) && b", "a && b", "a && b", "a && b", "a && b"},
			{ "(a || b) && b", "b", "b", "b", "b"},
			{ "a && (b || a)", "a", "a", "a", "a"},
			{ "a && (a || b)", "a", "a", "a", "a"},
			{ "a && (b || b)", "a && b", "a && b", "a && b", "a && b"},

			{ "!a || b && a", "!a || b", "!a || b", "!a || b", "!a || b"},
			{ "a || !b && a", "a", "a", "a", "a"},
			{ "a || b && !a", "a || b", "a || b", "a || b", "a || b"},
			{ "!a || !b && a", "!a || !b", "!a || !b", "!a || !b", "!a || !b"},
			{ "!a || b && !a", "!a", "!a", "!a", "!a"},
			{ "a || !b && !a", "!b || a", "!b || a", "!b || a", "!b || a"},
			{ "!a || !b && !a", "!a", "!a", "!a", "!a"},
			{ "(!a || b) && a", "a && b", "a && b", "a && b", "a && b"},
			{ "(a || !b) && a", "a", "a", "a", "a"},
			{ "(a || b) && !a", "!a && b", "!a && b", "!a && b", "!a && b"},
			{ "(!a || !b) && a", "!b && a", "!b && a", "!b && a", "!b && a"},
			{ "(!a || b) && !a", "!a", "!a", "!a", "!a"},
			{ "(a || !b) && !a", "!a && !b", "!a &&!b", "!a && !b", "!a && !b"},
			{ "(!a || !b) && !a", "!a", "!a", "!a", "!a"},

			{ "!(!a || b) && a", "!(!a || b) && a", "!b && a", "!b && a", "!b && a"},
			{ "!(!(!a || b) && a)", "!(!(!a || b) && a)", "!a || b", "!a || b", "!a || b"},
			{ "!(a || !b) && a", "!(!b || a) && a", "false", "false", "false"},
			{ "!(!(a || !b) && a)", "!(!(!b || a) && a)", "true", "true", "true"},
			{ "!(a || b) && !a", "!(a || b) && !a", "!a && !b", "!a && !b", "!a && !b"},
			{ "!(!(a || b) && !a)", "!(!(a || b) && !a)", "a || b", "a || b", "a || b"},
			{ "!(!a || !b) && a", "!(!a || !b) && a", "a && b", "a && b", "a && b"},
			{ "!(!(!a || !b) && a)", "!(!(!a || !b) && a)", "!a || !b", "!a || !b", "!a || !b"},
			{ "!(!a || b) && !a", "!(!a || b) && !a", "false", "false", "false"},
			{ "!(!(!a || b) && !a)", "!(!(!a || b) && !a)", "true", "true", "true"},
			{ "!(a || !b) && !a", "!(!b || a) && !a", "!a && b", "!a && b", "!a && b"},
			{ "!(!(a || !b) && !a)", "!(!(!b || a) && !a)", "!b || a", "!b || a", "!b || a"},
			{ "!(!a || !b) && !a", "!(!a || !b) && !a", "false", "false", "false"},
			{ "!(!(!a || !b) && !a)", "!(!(!a || !b) && !a)", "true", "true", "true"},

			{ "!a || a && b", "!a || b", "!a || b", "!a || b", "!a || b"},
			{ "a || !a && b", "a || b", "a || b", "a || b", "a || b"},
			{ "a || a && !b", "a", "a", "a", "a"},
			{ "!a || !a && b", "!a", "!a", "!a", "!a"},
			{ "!a || a && !b", "!a || !b", "!a || !b", "!a || !b", "!a || !b"},
			{ "a || !a && !b", "!b || a", "!b || a", "!b || a", "!b || a"},
			{ "!a || !a && !b", "!a", "!a", "!a", "!a"},
			{ "(!a || a) && b", "b", "b", "b", "b"},
			{ "(a || !a) && b", "b", "b", "b", "b"},
			{ "(a || a) && !b", "!b && a", "!b && a", "!b && a", "!b && a"},
			{ "(!a || !a) && b", "!a && b", "!a && b", "!a && b", "!a && b"},
			{ "(!a || a) && !b", "!b", "!b", "!b", "!b"},
			{ "(a || !a) && !b", "!b", "!b", "!b", "!b"},
			{ "(!a || !a) && !b", "!a && !b", "!a && !b", "!a && !b", "!a && !b"},

			{ "!(!a || a) && b", "false", "false", "false", "false"},
			{ "!(!(!a || a) && b)", "true", "true", "true", "true"},
			{ "!(a || !a) && b", "false", "false", "false", "false"},
			{ "!(!(a || !a) && b)", "true", "true", "true", "true"},
			{ "!(a || a) && !b", "!a && !b", "!a && !b", "!a && !b", "!a && !b"},
			{ "!(!(a || a) && !b)", "!(!a && !b)", "a || b", "a || b", "a || b"},
			{ "!(!a || !a) && b", "a && b", "a && b", "a && b", "a && b"},
			{ "!(!(!a || !a) && b)", "!(a && b)", "!a || !b", "!a || !b", "!a || !b"},
			{ "!(!a || a) && !b", "false", "false", "false", "false"},
			{ "!(!(!a || a) && !b)", "true", "true", "true", "true"},
			{ "!(a || !a) && !b", "false", "false", "false", "false"},
			{ "!(!(a || !a) && !b)", "true", "true", "true", "true"},
			{ "!(!a || !a) && !b", "!b && a", "!b && a", "!b && a", "!b && a"},

			{ "!a || b && b", "!a || b", "!a || b", "!a || b", "!a || b"},
			{ "a || !b && b", "a", "a", "a", "a"},
			{ "a || b && !b", "a", "a", "a", "a"},
			{ "!a || !b && b", "!a", "!a", "!a", "!a"},
			{ "!a || b && !b", "!a", "!a", "!a", "!a"},
			{ "a || !b && !b", "!b || a", "!b || a", "!b || a", "!b || a"},
			{ "!a || !b && !b", "!a || !b", "!a || !b", "!a || !b", "!a || !b"},
			{ "(!a || b) && b", "b", "b", "b", "b"},
			{ "(a || !b) && b", "a && b", "a && b", "a && b", "a && b"},
			{ "(a || b) && !b", "!b && a", "!b && a", "!b && a", "!b && a"},
			{ "(!a || !b) && b", "!a && b", "!a && b", "!a && b", "!a && b"},
			{ "(!a || b) && !b", "!a && !b", "!a && !b", "!a && !b", "!a && !b"},
			{ "(a || !b) && !b", "!b", "!b", "!b", "!b"},
			{ "(!a || !b) && !b", "!b", "!b", "!b", "!b"},


			{ "!(!a || b) && b", "!(!a || b) && b", "false", "false", "false"},
			{ "!(!(!a || b) && b)", "!(!(!a || b) && b)", "true", "true", "true"},
			{ "!(a || !b) && b", "!(!b || a) && b", "!a && b", "!a && b", "!a && b"},
			{ "!(!(a || !b) && b)", "!(!(!b || a) && b)", "!b || a", "!b || a", "!b || a"},
			{ "!(a || b) && !b", "!(a || b) && !b", "!a && !b", "!a && !b", "!a && !b"},
			{ "!(!(a || b) && !b)", "!(!(a || b) && !b)", "a || b", "a || b", "a || b"},
			{ "!(!a || !b) && b", "!(!a || !b) && b", "a && b", "a && b", "a && b"},
			{ "!(!(!a || !b) && b)", "!(!(!a || !b) && b)", "!a || !b", "!a || !b", "!a || !b"},
			{ "!(!a || b) && !b", "!(!a || b) && !b", "!b && a", "!b && a", "!b && a"},
			{ "!(!(!a || b) && !b)", "!(!(!a || b) && !b)", "!a || b", "!a || b", "!a || b"},
			{ "!(a || !b) && !b", "!(!b || a) && !b", "false", "false", "false"},
			{ "!(!(a || !b) && !b)", "!(!(!b || a) && !b)", "true", "true", "true"},
			{ "!(!a || !b) && !b", "!(!a || !b) && !b", "false", "false", "false"},
			{ "!(!(!a || !b) && !b)", "!(!(!a || !b) && !b)", "true", "true", "true"},


			{ "!a && b || a", "a || b", "a || b", "a || b", "a || b"},
			{ "a && !b || a", "a", "a", "a", "a"},
			{ "a && b || !a", "!a || b", "!a || b", "!a || b", "!a || b"},
			{ "!a && !b || a", "!b || a", "!b || a", "!b || a", "!b || a"},
			{ "!a && b || !a", "!a", "!a", "!a", "!a"},
			{ "a && !b || !a", "!a || !b", "!a || !b", "!a || !b", "!a || !b"},
			{ "!a && !b || !a", "!a", "!a", "!a", "!a"},
			{ "!a && (b || a)", "!a && b", "!a && b", "!a && b", "!a && b"},
			{ "a && (!b || a)", "a", "a", "a", "a"},
			{ "a && (b || !a)", "a && b", "a && b", "a && b", "a && b"},
			{ "!a && (!b || a)", "!a && !b", "!a && !b", "!a && !b", "!a && !b"},
			{ "!a && (b || !a)", "!a", "!a", "!a", "!a"},
			{ "a && (!b || !a)", "!b && a", "!b && a", "!b && a", "!b && a"},
			{ "!a && (!b || !a)", "!a", "!a", "!a", "!a"},

			{ "!a && !(b || a)", "!(a || b) && !a", "!a && !b", "!a && !b", "!a && !b"},
			{ "!(!a && !(b || a))", "!(!(a || b) && !a)", "a || b", "a || b", "a || b"},
			{ "a && !(b || !a)", "!(!a || b) && a", "!b && a", "!b && a", "!b && a"},
			{ "!(a && !(b || !a))", "!(!(!a || b) && a)", "!a || b", "!a || b", "!a || b"},
			{ "!a && !(!b || a)", "!(!b || a) && !a", "!a && b", "!a && b", "!a && b"},
			{ "!(!a && !(!b || a))", "!(!(!b || a) && !a)", "!b || a", "!b || a", "!b || a"},
			{ "!a && !(b || !a)", "!(!a || b) && !a", "false", "false", "false"},
			{ "!(!a && !(b || !a))", "!(!(!a || b) && !a)", "true", "true", "true"},
			{ "a && !(!b || !a)", "!(!a || !b) && a", "a && b", "a && b", "a && b"},
			{ "!(a && !(!b || !a))", "!(!(!a || !b) && a)", "!a || !b", "!a || !b", "!a || !b"},
			{ "!a && !(!b || !a)", "!(!a || !b) && !a", "false", "false", "false"},
			{ "!(!a && !(!b || !a))", "!(!(!a || !b) && !a)", "true", "true", "true"},

			{ "!a && a || b", "b", "b", "b", "b"},
			{ "a && !a || b", "b", "b", "b", "b"},
			{ "a && a || !b", "!b || a", "!b || a", "!b || a", "!b || a"},
			{ "!a && !a || b", "!a || b", "!a || b", "!a || b", "!a || b"},
			{ "!a && a || !b", "!b", "!b", "!b", "!b"},
			{ "a && !a || !b", "!b", "!b", "!b", "!b"},
			{ "!a && !a || !b", "!a || !b", "!a || !b", "!a || !b", "!a || !b"},
			{ "!a && (a || b)", "!a && b", "!a && b", "!a && b", "!a && b"},
			{ "a && (!a || b)", "a && b", "a && b", "a && b", "a && b"},
			{ "a && (a || !b)", "a", "a", "a", "a"},
			{ "!a && (!a || b)", "!a", "!a", "!a", "!a"},
			{ "!a && (a || !b)", "!a && !b", "!a && !b", "!a && !b", "!a && !b"},
			{ "a && (!a || !b)", "!b && a", "!b && a", "!b && a", "!b && a"},
			{ "!a && (!a || !b)", "!a", "!a", "!a", "!a"},

			{ "!a && !(a || b)", "!(a || b) && !a", "!a && !b", "!a && !b", "!a && !b"},
			{ "!(!a && !(a || b))", "!(!(a || b) && !a)", "a || b", "a || b", "a || b"},
			{ "a && !(!a || b)", "!(!a || b) && a", "!b && a", "!b && a", "!b && a"},
			{ "!(a && !(!a || b))", "!(!(!a || b) && a)", "!a || b", "!a || b", "!a || b"},
			{ "a && !(a || !b)", "!(!b || a) && a", "false", "false", "false"},
			{ "!(a && !(a || !b))", "!(!(!b || a) && a)", "true", "true", "true"},
			{ "!a && !(!a || b)", "!(!a || b) && !a", "false", "false", "false"},
			{ "!(!a && !(!a || b))", "!(!(!a || b) && !a)", "true", "true", "true"},
			{ "!a && !(a || !b)", "!(!b || a) && !a", "!a && b", "!a && b", "!a && b"},
			{ "!(!a && !(a || !b))", "!(!(!b || a) && !a)", "!b || a", "!b || a", "!b || a"},
			{ "a && !(!a || !b)", "!(!a || !b) && a", "a && b", "a && b", "a && b"},
			{ "!(a && !(!a || !b))", "!(!(!a || !b) && a)", "!a || !b", "!a || !b", "!a || !b"},
			{ "!a && !(!a || !b)", "!(!a || !b) && !a", "false", "false", "false"},
			{ "!(!a && !(!a || !b))", "!(!(!a || !b) && !a)", "true", "true", "true"},

			{ "!a && b || b", "b", "b", "b", "b"},
			{ "a && !b || b", "a || b", "a || b", "a || b", "a || b"},
			{ "a && b || !b", "!b || a", "!b || a", "!b || a", "!b || a"},
			{ "!a && !b || b", "!a || b", "!a || b", "!a || b", "!a || b"},
			{ "!a && b || !b", "!a || !b", "!a || !b", "!a || !b", "!a || !b"},
			{ "a && !b || !b", "!b", "!b", "!b", "!b"},
			{ "!a && !b || !b", "!b", "!b", "!b", "!b"},
			{ "!a && (b || b)", "!a && b", "!a && b", "!a && b", "!a && b"},
			{ "a && (!b || b)", "a", "a", "a", "a"},
			{ "a && (b || !b)", "a", "a", "a", "a"},
			{ "!a && (!b || b)", "!a", "!a", "!a", "!a"},
			{ "!a && (b || !b)", "!a", "!a", "!a", "!a"},
			{ "a && (!b || !b)", "!b && a", "!b && a", "!b && a", "!b && a"},
			{ "!a && (!b || !b)", "!a && !b", "!a && !b", "!a && !b", "!a && !b"},

			{ "!a && !(b || b)", "!a && !b", "!a && !b", "!a && !b", "!a && !b"},
			{ "!(!a && !(b || b))", "!(!a && !b)", "a || b", "a || b", "a || b"},
			{ "a && !(!b || b)", "false", "false", "false", "false"},
			{ "!(a && !(!b || b))", "true", "true", "true", "true"},
			{ "a && !(b || !b)", "false", "false", "false", "false"},
			{ "!(a && !(b || !b))", "true", "true", "true", "true"},
			{ "!a && !(!b || b)", "false", "false", "false", "false"},
			{ "!(!a && !(!b || b))", "true", "true", "true", "true"},
			{ "!a && !(b || !b)", "false", "false", "false", "false"},
			{ "!(!a && !(b || !b))", "true", "true", "true", "true"},
			{ "a && !(!b || !b)", "a && b", "a && b", "a && b", "a && b"},
			{ "!(a && !(!b || !b))", "!(a && b)", "!a || !b", "!a || !b", "!a || !b"},
			{ "!a && !(!b || !b)", "!a && b", "!a && b", "!a && b", "!a && b"},
			{ "!(!a && !(!b || !b))", "!(!a && b)", "!b || a", "!b || a", "!b || a"},

			{ "a || b && c", "b && c || a", "b && c || a", "(a || b) && (a || c)", "b && c || a"},
			{ "a || c && b", "b && c || a", "b && c || a", "(a || b) && (a || c)", "b && c || a"},
			{ "b || a && c", "a && c || b", "a && c || b", "(a || b) && (b || c)", "a && c || b"},
			{ "b || c && a", "a && c || b", "a && c || b", "(a || b) && (b || c)", "a && c || b"},
			{ "c || a && b", "a && b || c", "a && b || c", "(a || c) && (b || c)", "a && b || c"},
			{ "c || b && a", "a && b || c", "a && b || c", "(a || c) && (b || c)", "a && b || c"},
			{ "a && b || c", "a && b || c", "a && b || c", "(a || c) && (b || c)", "a && b || c"},
			{ "a && c || b", "a && c || b", "a && c || b", "(a || b) && (b || c)", "a && c || b"},
			{ "b && a || c", "a && b || c", "a && b || c", "(a || c) && (b || c)", "a && b || c"},
			{ "b && c || a", "b && c || a", "b && c || a", "(a || b) && (a || c)", "b && c || a"},
			{ "c && a || b", "a && c || b", "a && c || b", "(a || b) && (b || c)", "a && c || b"},
			{ "c && b || a", "b && c || a", "b && c || a", "(a || b) && (a || c)", "b && c || a"},

			{ "(a || b) && c", "(a || b) && c", "(a || b) && c", "(a || b) && c", "(a && c) || (b && c)"},
			{ "(a || c) && b", "(a || c) && b", "(a || c) && b", "(a || c) && b", "(a && b) || (b && c)"},
			{ "(b || a) && c", "(a || b) && c", "(a || b) && c", "(a || b) && c", "(a && c) || (b && c)"},
			{ "(b || c) && a", "(b || c) && a", "(b || c) && a", "(b || c) && a", "(a && b) || (a && c)"},
			{ "(c || a) && b", "(a || c) && b", "(a || c) && b", "(a || c) && b", "(a && b) || (b && c)"},
			{ "(c || b) && a", "(b || c) && a", "(b || c) && a", "(b || c) && a", "(a && b) || (a && c)"},
			{ "a && (b || c)", "(b || c) && a", "(b || c) && a", "(b || c) && a", "(a && b) || (a && c)"},
			{ "a && (c || b)", "(b || c) && a", "(b || c) && a", "(b || c) && a", "(a && b) || (a && c)"},
			{ "b && (a || c)", "(a || c) && b", "(a || c) && b", "(a || c) && b", "(a && b) || (b && c)"},
			{ "b && (c || a)", "(a || c) && b", "(a || c) && b", "(a || c) && b", "(a && b) || (b && c)"},
			{ "c && (a || b)", "(a || b) && c", "(a || b) && c", "(a || b) && c", "(a && c) || (b && c)"},
			{ "c && (b || a)", "(a || b) && c", "(a || b) && c", "(a || b) && c", "(a && c) || (b && c)"},

			{ "!(a || b) && c", "!(a || b) && c", "!a && !b && c", "!a && !b && c", "!a && !b && c"},
			{ "!(a || c) && b", "!(a || c) && b", "!a && !c && b", "!a && !c && b", "!a && !c && b"},
			{ "!(b || a) && c", "!(a || b) && c", "!a && !b && c", "!a && !b && c", "!a && !b && c"},
			{ "!(b || c) && a", "!(b || c) && a", "!b && !c && a", "!b && !c && a", "!b && !c && a"},
			{ "!(c || a) && b", "!(a || c) && b", "!a && !c && b", "!a && !c && b", "!a && !c && b"},
			{ "!(c || b) && a", "!(b || c) && a", "!b && !c && a", "!b && !c && a", "!b && !c && a"},
			{ "a && !(b || c)", "!(b || c) && a", "!b && !c && a", "!b && !c && a", "!b && !c && a"},
			{ "a && !(c || b)", "!(b || c) && a", "!b && !c && a", "!b && !c && a", "!b && !c && a"},
			{ "b && !(a || c)", "!(a || c) && b", "!a && !c && b", "!a && !c && b", "!a && !c && b"},
			{ "b && !(c || a)", "!(a || c) && b", "!a && !c && b", "!a && !c && b", "!a && !c && b"},
			{ "c && !(a || b)", "!(a || b) && c", "!a && !b && c", "!a && !b && c", "!a && !b && c"},
			{ "c && !(b || a)", "!(a || b) && c", "!a && !b && c", "!a && !b && c", "!a && !b && c"},

			{ "a || b && c || d", "b && c || a || d", "b && c || a || d", "(a || b || d) && (a || c || d)", "b && c || a || d"},
			{ "(a || b) && c || d", "(a || b) && c || d", "(a || b) && c || d", "(a || b || d) && (c || d)", "(a && c) || (b && c) || d"},
			{ "(a || b) && (c || d)", "(a || b) && (c || d)", "(a || b) && (c || d)", "(a || b) && (c || d)", "(a && c) || (a && d) || (b && c) || (b && d)"},
			{ "(a || b && c) || d", "b && c || a || d", "b && c || a || d", "(a || b || d) && (a || c || d)", "b && c || a || d"},
			{ "a || (b && c || d)", "b && c || a || d", "b && c || a || d", "(a || b || d) && (a || c || d)", "b && c || a || d"},
			{ "!(a || b && c) || d", "!(b && c || a) || d", "!a && (!b || !c) || d", "(!a || d) && (!b || !c || d)", "(!a && !b) || !a && !c || d"},
			{ "a || !(b && c || d)", "!(b && c || d) || a", "!d && (!b || !c) || a", "(!b || !c || a) && (!d || a)", "(!b && !d) || !c && !d || a"}, //reverse order in simplify??
			{ "a && b || c && d", "a && b || c && d", "a && b || c && d", "(a || c) && (a || d) && (b || c) && (b || d)", "a && b || c && d"},
			{ "a && (b || c) && d", "(b || c) && a && d", "(b || c) && a && d", "(b || c) && a && d", "(a && b && d) || (a && c && d)"},
			{ "(a && b|| c) && d", "(a && b || c) && d", "(a && b || c) && d", "(a || c) && (b || c) && d", "(a && b && d) || (c && d)"},
			{ "a && (b || c && d)", "(c && d || b) && a", "(c && d || b) && a", "(b || c) && (b || d) && a", "a && b || a && c && d"},
			{ "a && !(b || c) && d", "!(b || c) && a && d", "!b && !c && a && d", "!b && !c && a && d", "!b && !c && a && d"},
			{ "!(a && b || c) && d", "!(a && b || c) && d", "!c && (!a || !b) && d", "!c && (!a || !b) && d", "!a && !c && d || !b && !c && d"}, // reverse order in nnf & cnf??
			{ "a && !(b || c && d)", "!(c && d || b) && a", "!b && (!c || !d) && a", "!b && (!c || !d) && a", "!b && !c && a || !b && !d && a"}
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
		assertEquals(nnf, BoaNormalFormIntrinsics.nnf(e));
	}

	@Test
	public void testCNF() throws Exception {
		assertEquals(cnf, BoaNormalFormIntrinsics.cnf(e));
	}

	@Test
	public void testDNF() throws Exception {
		assertEquals(dnf, BoaNormalFormIntrinsics.dnf(e));
	}
}
