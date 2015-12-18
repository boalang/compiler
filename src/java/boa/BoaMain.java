/*
 * Copyright 2015, Hridesh Rajan, Robert Dyer,
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
package boa;

import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 * The main entry point for Boa-related tools.
 *
 * @author hridesh
 * 
 */
public class BoaMain {	
	public static void main(final String[] args) throws IOException {
		// parse the top-level command line options
		final Options options = new Options();
		options.addOption("p", "parse", false, "check a Boa program (parse & semantic check)");
		options.addOption("c", "compile", false, "compile a Boa program");
		options.addOption("e", "execute", false, "execute a Boa program");
		options.addOption("g", "generate", false, "generate a Boa dataset");

		final CommandLine cl;
		try {
		    	if(args.length == 0) {
		    	    printHelp(options, ""); 
		    	    return;
		    	} else {
		    	    cl = new PosixParser().parse(options, new String[]{args[0]});
		    	    String[] tempargs = new String[args.length-1];
		    	    System.arraycopy(args, 1, tempargs, 0, args.length-1);
		    	    if(cl.hasOption("c")) {
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
	private static final void printHelp (Options options, String message) {
	    	String header = "The most commonly used Boa options are:";
	    	String footer = "\nPlease report issues at http://www.github.com/boalang/";
	    	System.err.println(message);
		new HelpFormatter().printHelp("Boa", header, options, footer);
	}
}
