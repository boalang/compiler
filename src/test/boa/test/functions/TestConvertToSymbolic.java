/*
 * Copyright 2020, Robert Dyer, Bishal Neupane
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
import static boa.functions.BoaAstIntrinsics.prettyprint;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import boa.functions.BoaNormalFormIntrinsics;
import boa.types.Ast.Expression;

/**
 * Test converting expressions to use symbolic names.
 *
 * @author rdyer
 * @author bneupan
 */
@RunWith(Parameterized.class)
public class TestConvertToSymbolic {
	@Parameters(name = "{index}][{0}][rcv:{1}][args:{2}][=> {3}")
	public static Collection<String[]> expressions() {
		return Arrays.asList(new String[][] {
			{ "o.size() < start", "o", "start,end", "$RECEIVER$.size() < $ARG$0" },
			{ "o.size() < start", "o", "", "$RECEIVER$.size() < start" },
			{ "o.size() < start", "o2", "", "o.size() < start" },
			{ "o.size() < start", "o2", "x,y,z", "o.size() < start" },

			{ "o.size() < start", "o", "o.size()", "$ARG$0 < start,,$RECEIVER$.size() < start" },

			{ "o.size()", "o", "o", "$RECEIVER$.size(),,$ARG$0.size()" },
			{ "o.size()", "o", "o.size()", "$ARG$0,,$RECEIVER$.size()" },

            { "start.length", "", "start,mid,end", "$ARG$0.length"},
            { "mid + end", "", "start,mid,end", "$ARG$1 + $ARG$2"},
            { "mid++ < start", "", "start,mid,end", "$ARG$1++ < $ARG$0"},
			{ "o.o2.size() < start", "o", "o.size()", "$RECEIVER$.o2.size() < start" },
            { "start.length >= mid + end", "", "start,mid,end", "$ARG$0.length >= $ARG$1 + $ARG$2"},

            { "start.length >= mid + end", "", "start,mid,start.length", "$ARG$2 >= $ARG$1 + end,,$ARG$0.length >= $ARG$1 + end"},
		});
	}

	private Expression e = null;
	private Expression rcv = null;
	private Expression[] args = null;
	private Expression[] converted = null;

	public TestConvertToSymbolic(final String e, final String rcv, final String args, final String converted) {
		this.e = parseexpression(e);
		this.rcv = parseexpression(rcv);
		if (args.equals("")) {
			this.args = new Expression[0];
		} else {
			final String[] arguments = args.split(",");
			this.args = new Expression[arguments.length];
			for (int i = 0; i < arguments.length; i++)
				this.args[i] = parseexpression(arguments[i]);
		}
		final String[] results = converted.split(",,");
		this.converted = new Expression[results.length];
		for (int i = 0; i < results.length; i++)
			this.converted[i] = parseexpression(results[i]);
	}

	@Test
	public void testConversion() throws Exception {
		final Expression[] result = BoaNormalFormIntrinsics.convertToSymbolicName(e, rcv, args);
        /*
		for (int i = 0; i < result.length; i++)
            System.err.println(boa.functions.BoaAstIntrinsics.prettyprint(result[i]));
        System.err.println("----");
        */

		assertEquals(converted.length, result.length);
		for (int i = 0; i < converted.length; i++)
			assertEquals(prettyprint(converted[i]), prettyprint(result[i]));
	}
}
