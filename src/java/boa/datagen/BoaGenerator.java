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
package boa.datagen;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 * The main entry point for Boa tools for generating datasets.
 *
 * @author hridesh
 * 
 */
public class BoaGenerator {	
	public static void main(final String[] args) throws IOException {
	    System.out.println("TODO: integrate existing datageneration code here.");
		// parse the command line options
		final Options options = new Options();
		options.addOption("json", "json", true,
				".json files for metadata");
		options.addOption("jsonCache", "json", true,
				".jsonCache files to be stored");

		final CommandLine cl;
		try {
			cl = new PosixParser().parse(options, args);
		} catch (final org.apache.commons.cli.ParseException e) {
			System.err.println(e.getMessage());
			new HelpFormatter().printHelp("BoaCompiler", options);

			return;
		}
		
		if (cl.hasOption("json")) {
			final String GH_JSON_PATH = cl.getOptionValue("json");
			DefaultProperties.GH_JSON_PATH = GH_JSON_PATH;
		}
		
		if (cl.hasOption("jsonCache")) {
			final String GH_JSON_CACHE_PATH = cl.getOptionValue("jsonCache");
			DefaultProperties.GH_JSON_CACHE_PATH = GH_JSON_CACHE_PATH;
		}
		
		CacheGithubJSON.main(args);
		try {
			SeqRepoImporter.main(args);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		SeqProjectCombiner.main(args);
		SeqSort.main(args);
		SeqSortMerge.main(args);
		try {
			MapFileGen.main(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	private static final void printHelp (Options options, String message) {
	    	String header = "The most commonly used Boa options are:";
	    	String footer = "\nPlease report issues at http://www.github.com/boalang/";
	    	System.err.println(message);
		new HelpFormatter().printHelp("Boa", header, options, footer);
	}
}
