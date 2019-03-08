/*
 * Copyright 2019, Robert Dyer
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

import static boa.functions.BoaAstIntrinsics.prettyprint;
import static org.junit.Assert.assertEquals;
import static boa.functions.BoaAstIntrinsics.parse;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test prettyprint().
 *
 * @author rdyer
 */
@RunWith(Parameterized.class)
public class TestPrettyprint {
	private final static String CLASS_START = "class c {\n";
	private final static String CLASS_END = "}\n";
	private final static String STATEMENT_START = CLASS_START + "\tvoid m()\n\t{\n\t\t";
	private final static String STATEMENT_END = "\n\t}\n" + CLASS_END;

	@Parameters
	public static Collection<String[]> code() {
		return Arrays.asList(new String[][] {
				/* classes */
				{ "class c {\n}\n" }, 
				{ "public class c {\n}\n" }, 
				{ "class c extends d {\n}\n" },
				{ "class c implements i1 {\n}\n" }, 
				{ "class c implements i1, i2, i3 {\n}\n" },
				{ "abstract static final private class c extends d implements i1, i2, i3 {\n}\n" },

				/* methods */
				{ CLASS_START + "\tvoid m()\n\t{\n\t}\n" + CLASS_END },
				{ CLASS_START + "\tint m()\n\t{\n\t\treturn 1;\n\t}\n" + CLASS_END },

				/* statements */
				{ STATEMENT_START + "switch (f1) {" 
						+ indent(3) + "case 1:" 
						+ indent(3) + "f1 = 2;" 
						+ indent(3) + "default:"
						+ indent(3) + "break;"
						+ indent(2) + "}" + STATEMENT_END }, // SWITCH
				{ STATEMENT_START + "throw new RuntimeException(e);" + STATEMENT_END }, // THROW
				
				/* expressions */
				{ STATEMENT_START + "List<String> list = new ArrayList<String>();" + STATEMENT_END} // NEW
		});
	}

	private static String indent(int num) {
		String str = "\n";
		while (num-- > 0) 
			str += "\t"; 
		return str;
	}
	
	private String code;

	public TestPrettyprint(final String code) {
		this.code = code;
	}
	

	@Test()
	public void testPrettyprint() throws Exception {
		String expected = prettyprint(parse(code));
		System.out.println(expected);
		assertEquals(code, expected);
	}
}
