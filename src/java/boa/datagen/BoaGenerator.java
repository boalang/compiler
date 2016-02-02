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

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import boa.datagen.forges.github.GetGithubRepoByUser;

/**
 * The main entry point for Boa tools for generating datasets.
 *
 * @author hridesh
 * 
 */
public class BoaGenerator {
	public static void main(final String[] args) throws IOException {
		final Options options = new Options();
		BoaGenerator.addOptions(options);

		final CommandLine cl;
		try {
			cl = new PosixParser().parse(options, args);
		} catch (final org.apache.commons.cli.ParseException e) {
			System.err.println(e.getMessage());
			new HelpFormatter().printHelp("BoaCompiler", options);
			return;
		}
		BoaGenerator.handleCmdOptions(cl, options, args);

		CacheGithubJSON.main(args);
		try {
			SeqRepoImporter.main(args);
		} catch (InterruptedException e) {
			// TODO Auto-gene rated catch block
			e.printStackTrace();
		}

		SeqProjectCombiner.main(args);
		// SeqSort.main(args);
		SeqSortMerge.main(args);
		try {
			MapFileGen.main(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		clear(args[0]);
	}

	private static final void printHelp(Options options, String message) {	
		String header = "The most commonly used Boa options are:";
		String footer = "\nPlease report issues at http://www.github.com/boalang/";
		System.err.println(message);
		new HelpFormatter().printHelp("boa", header, options, footer);
	}

	private static final void printHelp(Options options) {
		String header = "The most commonly used Boa options are:";
		String footer = "\nPlease report issues at http://www.github.com/boalang/";
		new HelpFormatter().printHelp("boa", header, options, footer);
	}

	private static void addOptions(Options options) {
		options.addOption("inputJson", "json", true, ".json files for metadata");
		options.addOption("inputRepo", "json", true, ".json files for metadata");
		options.addOption("output", "json", true, ".jsonCache files to be stored");
		options.addOption("user", "json", true, ".json files for metadata");
		options.addOption("password", "json", true, ".json files for metadata");
		options.addOption("targetUser", "json", true, ".json files for metadata");
		options.addOption("targetRepo", "json", true, ".json files for metadata");
		options.addOption("help", "help", true, "help");
		options.addOption("user", "username", true, "help");
		options.addOption("repo", "repo name", true, "help");
	}

	private static void handleCmdOptions(CommandLine cl, Options options, final String[] args) {
		if (cl.hasOption("inputJson") && cl.hasOption("inputRepo") && cl.hasOption("output")) {
			DefaultProperties.GH_JSON_PATH = cl.getOptionValue("inputJson");
			DefaultProperties.GH_JSON_CACHE_PATH = cl.getOptionValue("output");
			// DefaultProperties.GH_GIT_PATH = GH_JSON_CACHE_PATH + "/github";
			DefaultProperties.GH_GIT_PATH = cl.getOptionValue("inputRepo");

		} else if (cl.hasOption("inputJson") && cl.hasOption("output")) {
			DefaultProperties.GH_JSON_PATH = cl.getOptionValue("inputJson");
			DefaultProperties.GH_JSON_CACHE_PATH = cl.getOptionValue("output");
		} else if (cl.hasOption("inputRepo") && cl.hasOption("output")) {
			DefaultProperties.GH_JSON_CACHE_PATH = cl.getOptionValue("output");
			DefaultProperties.GH_GIT_PATH = cl.getOptionValue("inputRepo");
			CacheGithubJSON.jsonAvailable=false;
		} else if (cl.hasOption("user") && cl.hasOption("password") && cl.hasOption("targetUser")
				&& cl.hasOption("targetRepo") && cl.hasOption("output")) {
			try {
				// because there is no input directory in this case, we need
				// to create one
				String GH_JSON_PATH = new java.io.File(".").getCanonicalPath();
				DefaultProperties.GH_JSON_PATH = GH_JSON_PATH + "/input";
				getGithubMetadata(DefaultProperties.GH_JSON_PATH, cl.getOptionValue("user"),
						cl.getOptionValue("password"), cl.getOptionValue("targetUser"),
						cl.getOptionValue("targetRepo"));

				// output directory
				final String GH_JSON_CACHE_PATH = cl.getOptionValue("output");
				DefaultProperties.GH_JSON_CACHE_PATH = GH_JSON_CACHE_PATH;
				DefaultProperties.GH_GIT_PATH = GH_JSON_CACHE_PATH + "/github";
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.err.println("User must specify the path of the repository. Please see --remote and --local options");
			printHelp(options);
		}
	}

	//
	private static void clear(String mode) {
		File buf_map = new File(DefaultProperties.GH_JSON_CACHE_PATH + "/buf-map");
		if (buf_map.exists()) {
			buf_map.delete();
		}
		// File clonedCode = new File(DefaultProperties.GH_JSON_CACHE_PATH +
		// "/github");
		// if (clonedCode.exists())
		// org.apache.commons.io.FileUtils.deleteQuietly(clonedCode);

		if ("remote".equals(mode)) {
			File inputDirectory = new File(DefaultProperties.GH_JSON_CACHE_PATH + "/buf-map");
			if (inputDirectory.exists())
				org.apache.commons.io.FileUtils.deleteQuietly(inputDirectory);
		}
	}

	private static void getGithubMetadata(String inputPath, String username, String password, String targetUser,
			String targetRepo) {
		String[] args = { inputPath, username, password, targetUser, targetRepo };
		GetGithubRepoByUser.main(args);
	}
}
