/*
 * Copyright 2019-2022, Robert Dyer, Hridesh Rajan, Huaiyao Ma,
 *                 Bowling Green State University,
 *                 University of Nebraska Board of Regents,
 *                 and Iowa State University of Science and Technology
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

import boa.functions.BoaAstIntrinsics;
import boa.types.Diff.ChangedFile;


/**
 * Test prettyprint().
 *
 * @author rdyer
 * @author huaiyao
 */
@RunWith(Parameterized.class)
public class TestPrettyprintJava {
	private final static String CLASS_START = "class c {";
	private final static String CLASS_END = "\n}\n";
	private final static String STATEMENT_START = CLASS_START + indent(1) + "void m()" + indent(1) + "{" + indent(2);
	private final static String STATEMENT_END = indent(1) + "}" + CLASS_END;

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

				/* enums */
				{ "enum E {"
						+ indent(1) + "NONE(\"None\"),"
						+ indent(1) + "ONE(\"One\"),"
						+ indent(1) + "TWO(T.NAME);"
						+ indent(1) + "String value;"
						+ indent(1) + "E(final String value)"
						+ indent(1) + "{"
							+ indent(2) + "this.value = value;" + STATEMENT_END },

				/* methods */
				{ CLASS_START
						+ indent(1) + "void m()"
						+ indent(1) + "{"
						+ indent(1) + "}" + CLASS_END },
				{ CLASS_START
						+ indent(1) + "int m()"
						+ indent(1) + "{"
							+ indent(2) + "return 1;"
						+ indent(1) + "}" + CLASS_END },

				/* statements */
				{ STATEMENT_START + "switch (f1) {"
							+ indent(3) + "case 1:"
							+ indent(3) + "f1 = 2;"
							+ indent(3) + "default:"
							+ indent(3) + "break;"
						+ indent(2) + "}" + STATEMENT_END }, // SWITCH
				{ STATEMENT_START + "throw new RuntimeException(e);" + STATEMENT_END }, // THROW
				{ STATEMENT_START + "var s = \"this is a string\";" + STATEMENT_END }, // LOCAL VAR INFERENCE

				/* expressions */
				{ STATEMENT_START + "List<String> list = new ArrayList<String>();" + STATEMENT_END }, // NEW
				{ STATEMENT_START + "Func f = (E) -> {"
							+ indent(3) + "x = 2 * x;"
							+ indent(3) + "System.out.println(x);"
						+ indent(2) + "};" + STATEMENT_END }, // LAMBDA 1
				{ STATEMENT_START + "Func f = (int x, String y) -> {"
							+ indent(3) + "x = 2 * x;"
							+ indent(3) + "System.out.println(x);"
						+ indent(2) + "};" + STATEMENT_END }, // LAMBDA 2
				{ STATEMENT_START + "Sayable s = () -> {"
							+ indent(3) + "return \"I have nothing to day\";"
						+ indent(2) + "};" + STATEMENT_END }, // LAMBDA 3
				{ STATEMENT_START + "for (String s : strs)"
						+ indent(2) + "{"
							+ indent(3) + "System.out.println(s);"
						+ indent(2) + "}" + STATEMENT_END }, // FOREACH

				/*module declaration*/
//				{"open module com.bytestree.calculator {\n"
//						+ indent(1) + "	requires com.bytestree.maths;\n"
//						+ "}"},

				{ STATEMENT_START + "int season = switch (month) {"
							+ indent(3) + "case JAN:"
							+ indent(3) + "yield 1;"
							+ indent(3) + "case APRIL:"
							+ indent(3) + "yield 2;"
						+ indent(2) + "};" + STATEMENT_END },

				{ STATEMENT_START + "int season = switch (month) {"
							+ indent(3) + "case JAN -> 1;"
							+ indent(3) + "case APRIL -> 2;"
						+ indent(2) + "};" + STATEMENT_END }
		});
	}

	private static String indent(int num) {
		String str = "\n";
		while (num-- > 0)
			str += "\t";
		return str;
	}

	private String code;

	public TestPrettyprintJava(final String code) {
		this.code = code;
	}


	@Test()
	public void testPrettyprint() throws Exception {
		BoaAstIntrinsics.setlang(ChangedFile.FileKind.SOURCE_JAVA_JLS15);
		String expected = prettyprint(parse(code));
        System.err.println(code);
        System.err.println(expected);
        System.err.println("----------");
		assertEquals(code, expected);
	}
}
