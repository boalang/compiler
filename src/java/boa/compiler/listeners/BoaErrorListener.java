/*
 * Copyright 2015, Robert Dyer, Hridesh Rajan
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

package boa.compiler.listeners;

import java.io.File;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

public abstract class BoaErrorListener extends BaseErrorListener {
	public boolean hasError = false;

	public void error(final String kind, final TokenSource tokens, final Object offendingSymbol, final int line, final int charPositionInLine, final int length, final String msg, final Exception e) {
		hasError = true;

		final String filename = tokens.getSourceName();

		System.err.print(filename.substring(filename.lastIndexOf(File.separator) + 1) + ": compilation failed: ");
		System.err.print("Encountered " + kind + " error ");
		if (offendingSymbol != null)
			System.err.print("\"" + offendingSymbol + "\" ");
		System.err.print("at line " + line + ", ");
		if (length > 0)
			System.err.print("columns " + charPositionInLine + "-" + (charPositionInLine + length - 1));
		else
			System.err.print("column " + charPositionInLine);
		System.err.println(". " + msg);

		underlineError(tokens, (Token)offendingSymbol, line, charPositionInLine, length);

		if (e != null)
			for (final StackTraceElement st : e.getStackTrace())
				System.err.println("\tat " + st);
		else
			System.err.println("\tat unknown stack");
	}
	private void underlineError(final TokenSource tokens, final Token offendingToken, final int line, final int charPositionInLine, final int length) {
		final String input = tokens.getInputStream().toString() + "\n ";
		final String[] lines = input.split("\n");
		final String errorLine = lines[line - 1];
		System.err.println(errorLine.replaceAll("\t", "    "));

		int stop = Math.min(charPositionInLine, errorLine.length());
		for (int i = 0; i < stop; i++)
			if (errorLine.charAt(i) == '\t')
				System.err.print("    ");
			else
				System.err.print(" ");

		int stop2 = Math.min(stop + length, errorLine.length());
		for (int i = stop; i < stop2; i++)
			if (errorLine.charAt(i) == '\t')
				System.err.print("^^^^");
			else
				System.err.print("^");

		System.err.println();
	}
}
