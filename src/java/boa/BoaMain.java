/*
 * Copyright 2017, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology
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
package boa;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 * The main entry point for Boa-related tools.
 *
 * @author hridesh
 * @author rdyer
 */
public class BoaMain {
    public static void main(final String[] args) throws IOException {
        final Options options = new Options();

        options.addOption("p", "parse",    false, "parse and semantic check a Boa program (don't generate code)");
        options.addOption("c", "compile",  false, "compile a Boa program");
        options.addOption("e", "execute",  false, "execute a Boa program locally");
        options.addOption("g", "generate", false, "generate a Boa dataset");

        try {
            if (args.length == 0) {
                printHelp(options, null);
                return;
            } else {
                final CommandLine cl = new PosixParser().parse(options, new String[] { args[0] });
                final String[] tempargs = new String[args.length - 1];
                System.arraycopy(args, 1, tempargs, 0, args.length - 1);

                if (cl.hasOption("c")) {
                    boa.compiler.BoaCompiler.main(tempargs);
                } else if (cl.hasOption("p")) {
                    boa.compiler.BoaCompiler.parseOnly(tempargs);
                } else if (cl.hasOption("e")) {
                    boa.evaluator.BoaEvaluator.main(tempargs);
                } else if (cl.hasOption("g")) {
                    boa.datagen.BoaGenerator.main(tempargs);
                }
            }
        } catch (final org.apache.commons.cli.ParseException e) {
            printHelp(options, e.getMessage());
        }
    }

    protected static final void printHelp (final Options options, final String message) {
        if (message != null) System.err.println(message);

        final HelpFormatter help = new HelpFormatter();

		final PrintWriter pw = new PrintWriter(System.out);
		help.printWrapped(pw, HelpFormatter.DEFAULT_WIDTH, "The available options are:");
		help.printOptions(pw, HelpFormatter.DEFAULT_WIDTH, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD);
		help.printWrapped(pw, HelpFormatter.DEFAULT_WIDTH, "\nPlease report issues at http://www.github.com/boalang/compiler");
		pw.flush();
    }

	protected static String pascalCase(final String string) {
		final StringBuilder pascalized = new StringBuilder();

		boolean upper = true;
		for (final char c : string.toCharArray())
			if (Character.isDigit(c) || c == '_') {
				pascalized.append(c);
				upper = true;
			} else if (!Character.isDigit(c) && !Character.isLetter(c)) {
				upper = true;
			} else if (Character.isLetter(c)) {
				pascalized.append(upper ? Character.toUpperCase(c) : c);
				upper = false;
			}

		return pascalized.toString();
	}

	protected static String jarToClassname(final String path) {
		return jarToClassname(new File(path));
	}

	protected static String jarToClassname(final File f) {
		String s = f.getName();
		if (s.indexOf('.') != -1)
			s = s.substring(0, s.lastIndexOf('.'));
		return pascalCase(s);
	}
}
