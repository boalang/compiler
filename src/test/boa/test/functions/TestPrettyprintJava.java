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

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import boa.functions.BoaAstIntrinsics;
import boa.types.Diff.ChangedFile;


/**
 * Test prettyprint() for Java.
 *
 * @author rdyer
 * @author huaiyao
 */
@RunWith(Parameterized.class)
public class TestPrettyprintJava extends PrettyprintBase {
	private final static String CLASS_START = "class c {";
	private final static String CLASS_END = "\n}\n";

	private final static String METHOD_START = CLASS_START + indent(1) + "void m()" + indent(1) + "{";
	private final static String METHOD_END = indent(1) + "}" + CLASS_END;

	private final static String STATEMENT_START = METHOD_START + indent(2);
	private final static String STATEMENT_END = METHOD_END;

	@Parameters(name = "{0}")
	public static Collection<String[]> code() {
		return Arrays.asList(new String[][] {
				/* classes */
				{ "CLASS1", CLASS_START + CLASS_END },
				{ "CLASS2", "public " + CLASS_START + CLASS_END },
				{ "CLASS3", "class c extends d {" + CLASS_END },
				{ "CLASS4", "class c implements i1 {" + CLASS_END },
				{ "CLASS5", "class c implements i1, i2, i3 {" + CLASS_END },
				{ "CLASS6", "abstract static final private class c extends d implements i1, i2, i3 {" + CLASS_END },

				/* enums */
				{ "ENUM", "enum E {"
						+ indent(1) + "NONE(\"None\"),"
						+ indent(1) + "ONE(\"One\"),"
						+ indent(1) + "TWO(T.NAME);"
						+ indent(1) + "String value;"
						+ indent(1) + "E(final String value)"
						+ indent(1) + "{"
							+ indent(2) + "this.value = value;" + METHOD_END },

				/*module declaration*/
//				{ "MODULE", "open module com.bytestree.calculator {"
//						+ indent(1) + "	requires com.bytestree.maths;"
//						+ "\n}\n" },

				/* methods */
				{ "METHOD", METHOD_START + METHOD_END },

				/* statements */
				{ "EMPTY", STATEMENT_START + ";" + STATEMENT_END },
				{ "BLOCK", STATEMENT_START + "{"
						+ indent(2) + "}" + STATEMENT_END },
				{ "RETURN1", STATEMENT_START + "return;" + STATEMENT_END },
				{ "RETURN2", STATEMENT_START + "return 1;" + STATEMENT_END },
				{ "BREAK1", STATEMENT_START + "break;" + STATEMENT_END },
				{ "BREAK2", STATEMENT_START + "break LABEL;" + STATEMENT_END },
				{ "CONTINUE1", STATEMENT_START + "continue;" + STATEMENT_END },
				{ "CONTINUE2", STATEMENT_START + "continue LABEL;" + STATEMENT_END },
				{ "ASSERT1", STATEMENT_START + "assert true;" + STATEMENT_END },
				{ "ASSERT2", STATEMENT_START + "assert true : \"some message\";" + STATEMENT_END },
				{ "LABEL", STATEMENT_START + "FOO: ;" + STATEMENT_END },
				{ "SWITCH", STATEMENT_START + "switch (f1) {"
							+ indent(3) + "case 1:"
							+ indent(3) + "f1 = 2;"
							+ indent(3) + "default:"
							+ indent(3) + "break;"
						+ indent(2) + "}" + STATEMENT_END },
				{ "EXPRESSION", STATEMENT_START + "x++;" + STATEMENT_END },
				{ "TYPEDECL", STATEMENT_START + "\t\tclass d {" + indent(2) + "}\n" + STATEMENT_END },
				{ "SYNCHRONIZED", STATEMENT_START + "synchronized (o) {"
							+ indent(3) + ";"
						+ indent(2) + "}" + STATEMENT_END },
				{ "TRY/CATCH/FINALLY", STATEMENT_START + "try {"
							+ indent(3) + ";"
						+ indent(2) + "}"
						+ indent(2) + "catch (Exception e) {"
							+ indent(3) + ";"
						+ indent(2) + "}"
						+ indent(2) + "finally {"
							+ indent(3) + ";"
						+ indent(2) + "}\n" + STATEMENT_END },
				{ "FOR", STATEMENT_START + "for (int i = 0; i < l; i++)"
							+ indent(3) + "{"
							+ indent(4) + ";"
						+ indent(3) + "}\n" + STATEMENT_END },
				{ "FOREACH", STATEMENT_START + "for (String s : strs)"
						+ indent(3) + "{"
							+ indent(4) + ";"
						+ indent(3) + "}" + STATEMENT_END },
				{ "WHILE", STATEMENT_START + "while (x)"
							+ indent(3) + "{"
							+ indent(4) + ";"
						+ indent(3) + "}" + STATEMENT_END },
				{ "THROW", STATEMENT_START + "throw new RuntimeException(e);" + STATEMENT_END },
				{ "VARINF", STATEMENT_START + "var s = \"this is a string\";" + STATEMENT_END },

				/* expressions */
				{ "NEW", STATEMENT_START + "List<String> list = new ArrayList<String>();" + STATEMENT_END },
				{ "LAMBDA1", STATEMENT_START + "Func f = (E) -> {"
							+ indent(3) + "x = 2 * x;"
							+ indent(3) + "System.out.println(x);"
						+ indent(2) + "};" + STATEMENT_END },
				{ "LAMBDA2", STATEMENT_START + "Func f = (int x, String y) -> {"
							+ indent(3) + "x = 2 * x;"
							+ indent(3) + "System.out.println(x);"
						+ indent(2) + "};" + STATEMENT_END },
				{ "LAMBDA3", STATEMENT_START + "Sayable s = () -> {"
							+ indent(3) + "return \"I have nothing to day\";"
						+ indent(2) + "};" + STATEMENT_END },
				{ "SWITCHEXP1", STATEMENT_START + "int season = switch (month) {"
							+ indent(3) + "case JAN:"
							+ indent(3) + "yield 1;"
							+ indent(3) + "case APRIL:"
							+ indent(3) + "yield 2;"
						+ indent(2) + "};" + STATEMENT_END },
				{ "SWITCHEXP2", STATEMENT_START + "int season = switch (month) {"
							+ indent(3) + "case JAN -> 1;"
							+ indent(3) + "case APRIL -> 2;"
						+ indent(2) + "};" + STATEMENT_END },
		});
	}

	public TestPrettyprintJava(final String name, final String code) {
        super(name, code);
		BoaAstIntrinsics.setlang(ChangedFile.FileKind.SOURCE_JAVA_JLS15);
	}
}
