/*
 * Copyright 2022, Robert Dyer,
 *                 and University of Nebraska Board of Regents,
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

import static boa.functions.BoaAstIntrinsics.prettyprint;
import static org.junit.Assert.assertEquals;
import static boa.functions.BoaAstIntrinsics.parse;

import org.junit.Test;


/**
 * Base class for prettyprint() tests.
 *
 * @author rdyer
 */
public class PrettyprintBase {
	protected static String indent(int num) {
		String str = "\n";
		while (num-- > 0)
			str += "\t";
		return str;
	}

	private String code;

	public PrettyprintBase(final String name, final String code) {
		this.code = code;
	}

	@Test()
	public void testPrettyprint() throws Exception {
		assertEquals(code, prettyprint(parse(code)));
	}
}
