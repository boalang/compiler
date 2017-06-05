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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import boa.functions.BoaAstIntrinsics;
import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;

/**
 * Tests the literal testing functions.
 * The data consists of literal strings, and a number from 1-7 indicating the
 * kind of literal.
 *
 * @author rdyer
 */
@RunWith(Parameterized.class)
public class TestLiterals {
	private Expression e = null;
	private Integer k = -1;

	@Parameters(name = "{index}][{0}")
	public static Collection literals() {
		return Arrays.asList(new Object[][] {
			// integers
			{ "5", 1 },
			{ "5L", 1 },
			{ "5l", 1 },
			{ "0x7fff_ffff", 1 },
			{ "0177_7777_7777", 1 },
			{ "0b0111_1111_1111_1111_1111_1111_1111_1111", 1 },
			{ "0x8000_0000", 1 },
			{ "0200_0000_0000", 1 },
			{ "0b1000_0000_0000_0000_0000_0000_0000_0000", 1 },
			{ "0xffff_ffff", 1 },
			{ "0377_7777_7777", 1 },
			{ "0b1111_1111_1111_1111_1111_1111_1111_1111", 1 },
			{ "0x7fff_ffff_ffff_ffffL", 1 },
			{ "07_7777_7777_7777_7777_7777L", 1 },
			{ "0b0111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111L", 1 },
			{ "0x8000_0000_0000_0000L", 1 },
			{ "010_0000_0000_0000_0000_0000L", 1 },
			{ "0b1000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000L", 1 },
			{ "0xffff_ffff_ffff_ffffL", 1 },
			{ "017_7777_7777_7777_7777_7777L", 1 },
			{ "0b1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111L", 1 },

			// floats
			{ "1.0", 2 },
			{ "1e1f", 2 },
			{ "2.f", 2 },
			{ ".3f", 2 },
			{ "0f", 2 },
			{ "3.14f", 2 },
			{ "6.022137e+23f", 2 },
			{ "1e1", 2 },
			{ "2.", 2 },
			{ ".3", 2 },
			{ "0.0", 2 },
			{ "3.14", 2 },
			{ "1e-9d", 2 },
			{ "1e137", 2 },

			// characters
			{ "'a'", 3 },
			{ "'%'", 3 },
			{ "'\t'", 3 },
			{ "'\\n'", 3 },
			{ "'\\'", 3 },
			{ "'\\''", 3 },
			{ "'\"'", 3 },
			{ "'\u03a9'", 3 },
			{ "'\uFFFF'", 3 },
			{ "'\177'", 3 },
			{ "'™'", 3 },

			// strings
			{ "\"\"", 4 },
			{ "\"\"\"", 4 },
			{ "\"\\\"\"", 4 },
			{ "\"test\"", 4 },
			{ "\"test string \"with quotes\"\"", 4 },

			// types
			{ "C.class", 5 },
			{ "A.B.C.class", 5 },

			// boolean
			{ "true", 6 },
			{ "false", 6 },

			// null
			{ "null", 7 },
		});
	}

	public TestLiterals(final String s, final Integer kind) {
		final Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.LITERAL);
		b.setLiteral(s);
		this.e = b.build();
		this.k = kind;
	}

	@Test
	public void testIntLiteral() throws Exception {
		assertEquals(k == 1, BoaAstIntrinsics.isIntLit(e));
	}

	@Test
	public void testFloatLiteral() throws Exception {
		assertEquals(k == 2, BoaAstIntrinsics.isFloatLit(e));
	}

	@Test
	public void testCharLiteral() throws Exception {
		assertEquals(k == 3, BoaAstIntrinsics.isCharLit(e));
	}

	@Test
	public void testStringLiteral() throws Exception {
		assertEquals(k == 4, BoaAstIntrinsics.isStringLit(e));
	}

	@Test
	public void testTypeLiteral() throws Exception {
		assertEquals(k == 5, BoaAstIntrinsics.isTypeLit(e));
	}

	@Test
	public void testBoolLiteral() throws Exception {
		assertEquals(k == 6, BoaAstIntrinsics.isBoolLit(e));
	}

	@Test
	public void testNullLiteral() throws Exception {
		assertEquals(k == 7, BoaAstIntrinsics.isNullLit(e));
	}
}
