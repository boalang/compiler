/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
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
package boa.test.compiler;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author rdyer
 */
@RunWith(JUnit4.class)
public class TestParserBad extends BaseTest {
	final private static String rootDir = "test/parsing/";
	final private static String badDir = rootDir + "errors/";


	@Test
	public void empty() throws IOException {
		parse(load(badDir + "empty.boa"),
			new String[] { "1,0: no viable alternative at input '<EOF>'" });
	}

	@Test
	public void keywordAsId() throws IOException {
		parse(load(badDir + "keyword-as-id.boa"),
			new String[] { "2,7: keyword 'output' can not be used as an identifier" });
	}

	@Test
	public void badEmitWithWeight() throws IOException {
		parse(load(badDir + "bad-emit-with-weight.boa"),
			new String[] { "1,5: error: expected 'expression ' before keyword 'weight'" });
	}

	@Test
	public void badIdentifiers() throws IOException {
		parse(load(badDir + "bad-identifiers.boa"),
			new String[] {
				"2,0: extraneous input '_' expecting {'of', 'if', 'do', 'map', 'stack', 'set', 'for', 'foreach', 'ifall', 'exists', 'not', 'type', 'else', 'case', 'output', 'format', 'while', 'break', 'array', 'static', 'switch', 'return', 'weight', 'default', 'continue', 'function', 'visitor', 'before', 'after', 'stop', ';', '{', '(', '+', '-', '~', '!', '$', IntegerLiteral, FloatingPointLiteral, CharacterLiteral, RegexLiteral, StringLiteral, TimeLiteral, Identifier}",
				"3,1: error: ';' expected",
				"4,9: no viable alternative at input '+id:'",
				"4,5: error: ';' expected",
				"4,9: mismatched input ':' expecting {<EOF>, 'of', 'if', 'do', 'map', 'stack', 'set', 'for', 'foreach', 'ifall', 'exists', 'not', 'type', 'else', 'case', 'output', 'format', 'while', 'break', 'array', 'static', 'switch', 'return', 'weight', 'default', 'continue', 'function', 'visitor', 'before', 'after', 'stop', ';', '.', '{', '(', '[', 'or', '|', '||', 'and', '&', '&&', '+', '-', '^', '*', '/', '%', '>>', '~', '!', '$', '<<', IntegerLiteral, FloatingPointLiteral, CharacterLiteral, RegexLiteral, StringLiteral, TimeLiteral, Identifier}",
				"4,12: error: ';' expected"
			});
	}

	@Test
	public void outputVarEquals() throws IOException {
		parse(load(badDir + "output-var-equals.boa"),
			new String[] { "1,5: error: output variable declarations should not include '='" });
	}

	@Test
	public void assignToMap() throws IOException {
		parse(load(badDir + "assign-to-map.boa"),
			new String[] { "2,9: extraneous input ':' expecting {<EOF>, 'of', 'if', 'do', 'map', 'stack', 'set', 'for', 'foreach', 'ifall', 'exists', 'not', 'type', 'else', 'case', 'output', 'format', 'while', 'break', 'array', 'static', 'switch', 'return', 'weight', 'default', 'continue', 'function', 'visitor', 'before', 'after', 'stop', ';', '.', '{', '(', '[', 'or', '|', '||', 'and', '&', '&&', '+', '-', '^', '*', '/', '%', '>>', '~', '!', '$', '<<', IntegerLiteral, FloatingPointLiteral, CharacterLiteral, RegexLiteral, StringLiteral, TimeLiteral, Identifier}",
				"2,12: error: ';' expected"
			});
	}

	@Test
	public void visitorMissingBefore() throws IOException {
		parse(load(badDir + "visitor-missing-before.boa"),
			new String[] { "2,1: error: visit statements must start with 'before' or 'after'",
				"3,1: error: visit statements must start with 'before' or 'after'"
			});
	}
}
