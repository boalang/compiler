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

import boa.parser.BoaLexer;

/**
 * @author rdyer
 */
@RunWith(JUnit4.class)
public class TestLexerGood extends BaseTest {
	final private static String rootDir = "test/lexing/";


	@Test
	public void empty() throws IOException {
		lex(load(rootDir + "empty.boa"),
			new int[] { BoaLexer.EOF },
			new String[] { "<EOF>" });
	}

	@Test
	public void commentEOFnoEOL() throws IOException {
		lex(load(rootDir + "comment-eof-no-eol.boa"),
			new int[] { BoaLexer.EOF },
			new String[] { "<EOF>" });
	}

	@Test
	public void integerLiterals() throws IOException {
		lex(load(rootDir + "int-lit.boa"),
			new int[] { BoaLexer.IntegerLiteral, BoaLexer.IntegerLiteral, BoaLexer.IntegerLiteral, BoaLexer.IntegerLiteral, BoaLexer.IntegerLiteral, BoaLexer.IntegerLiteral, BoaLexer.IntegerLiteral, BoaLexer.IntegerLiteral, BoaLexer.IntegerLiteral, BoaLexer.IntegerLiteral, BoaLexer.IntegerLiteral, BoaLexer.IntegerLiteral, BoaLexer.IntegerLiteral, BoaLexer.IntegerLiteral, BoaLexer.EOF },
			new String[] { "7", "94", "50", "0x9aF", "0X9Af", "0x9", "0X9", "0x000", "0753", "0", "0b10", "0B10", "0b0", "0B1", "<EOF>" });
	}

	@Test
	public void floatingLiterals() throws IOException {
		lex(load(rootDir + "fp-lit.boa"),
			new int[] { BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.FloatingPointLiteral, BoaLexer.EOF },
			new String[] { "05.", "5.", "5.55", "5.e5", "5.e+5", "5.e-5", "5.E-5", "5.E5", ".5", ".5e0", ".5e+0", ".5e-0", ".5E0", "000e0", "000E0", "000E-0", "000E+0", "<EOF>" });
	}

	@Test
	public void charLiterals() throws IOException {
		lex(load(rootDir + "char-lit.boa"),
			new int[] { BoaLexer.CharacterLiteral, BoaLexer.CharacterLiteral, BoaLexer.CharacterLiteral, BoaLexer.CharacterLiteral, BoaLexer.CharacterLiteral, BoaLexer.CharacterLiteral, BoaLexer.CharacterLiteral, BoaLexer.CharacterLiteral, BoaLexer.CharacterLiteral, BoaLexer.CharacterLiteral, BoaLexer.CharacterLiteral, BoaLexer.CharacterLiteral, BoaLexer.CharacterLiteral, BoaLexer.CharacterLiteral, BoaLexer.CharacterLiteral, BoaLexer.EOF },
			new String[] { "'a'", "'\\n'", "'\\r'", "'\\t'", "'\\b'", "'\\f'", "'	'", "' '", "'\\\\'", "'\\''", "'\"'", "'\\\"'", "'\\0'", "'\\00'", "'\\000'", "<EOF>" });
	}

	@Test
	public void stringLiterals() throws IOException {

		lex(load(rootDir + "string-lit.boa"),
			new int[] { BoaLexer.StringLiteral, BoaLexer.StringLiteral, BoaLexer.StringLiteral, BoaLexer.StringLiteral, BoaLexer.StringLiteral, BoaLexer.StringLiteral, BoaLexer.StringLiteral, BoaLexer.StringLiteral, BoaLexer.StringLiteral, BoaLexer.StringLiteral, BoaLexer.StringLiteral, BoaLexer.StringLiteral, BoaLexer.StringLiteral, BoaLexer.EOF },
			new String[] { "\"\"", "\"	 !@#$%^&*()-_=+[]{};:',.<>/?|`~1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\"", "\"\\r\"", "\"\\n\"", "\"\\b\"", "\"\\t\"", "\"\\f\"", "\"\\\"\"", "\"\\'\"", "\"\\\\\"", "\"\\7\"", "\"\\77\"", "\"\\77\"", "<EOF>" });
	}

	@Test
	public void regexLiterals() throws IOException {
		lex(load(rootDir + "regex-lit.boa"),
			new int[] { BoaLexer.RegexLiteral, BoaLexer.RegexLiteral, BoaLexer.EOF },
			new String[] { "``", "`\\n\"\\r\\\\\\	 !@#$%^&*()-_=+[]{};:',.<>/?|~1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ`", "<EOF>" });
	}

	@Test
	public void identifierLiterals() throws IOException {
		lex(load(rootDir + "identifier-lit.boa"),
			new int[] { BoaLexer.Identifier, BoaLexer.Identifier, BoaLexer.Identifier, BoaLexer.Identifier, BoaLexer.EOF },
			new String[] { "valid_", "valid0", "valid0_", "Valid_0_valid", "<EOF>" });
	}

	@Test
	public void timeLiterals() throws IOException {
		lex(load(rootDir + "time-lit.boa"),
			new int[] { BoaLexer.TimeLiteral, BoaLexer.TimeLiteral, BoaLexer.TimeLiteral, BoaLexer.TimeLiteral, BoaLexer.EOF },
			new String[] { "0t", "1000000T", "T\"Wed Feb  4 16:26:41 PST 2004\"", "T\"Tue Jun  5 10:43:07 America/Los_Angeles 2007\"", "<EOF>" });
	}
}
