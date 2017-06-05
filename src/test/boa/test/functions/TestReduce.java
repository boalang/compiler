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
import boa.types.Ast.Expression.ExpressionKind;

/**
 * Test the expression reducer.
 *
 * @author rdyer
 */
@RunWith(Parameterized.class)
public class TestReduce {
	@Parameters(name = "{index}][{0}")
	public static Collection expressions() {
		return Arrays.asList(new Object[][] {
			// literals
			{ "5", "5" },
			{ "8.0", "8.0" },
			{ "-8.0", "-8.0" },
			{ "+2", "2" },

			// add operator
			{ "5 + 2 + 1", "8" },
			{ "5.0 + 2 + 1", "8.0" },
			{ "5.0 + x + 1", "6.0 + x" },

			// subtract operator
			{ "2 - -5 - 1", "6" },
			{ "5.0 - 2 - 1", "2.0" },
			{ "5.0 - x - 1", "4.0 - x" },
			{ "1 - x - 5", "-4 - x" },

			// multiply operator
			{ "2 * 5 * 1", "10" },
			{ "5.0 * 2 * 1", "10.0" },
			{ "5.0 * x * 1", "5.0 * x" },

			// divide operator
			{ "12 / 2 / 3", "2" },
			{ "10.0 / 2 / 1", "5.0" },
			{ "5.0 / x / 5", "1.0 / x" },

			// complex expressions
			{ "5 - 3 + 2", "4" },
			{ "5 - (3 + 2)", "0" },
			{ "5.0 / x / 5 * 10.0 * x", "1 * (10.0 * (1.0 / x)) * x" }, // FIXME should be 10
		});
	}

	private Expression e = null;
	private Expression reduced = null;

	public TestReduce(final String e, final String reduced) {
		this.e = parseexpression(e);
		this.reduced = parseexpression(reduced);
	}

	@Test
	public void testReduce() throws Exception {
		assertEquals(reduced, BoaNormalFormIntrinsics.reduce(e));
	}
}
