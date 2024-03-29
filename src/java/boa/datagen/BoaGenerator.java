/*
 * Copyright 2015-2022, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology
 *                 and University of Nebraska Board of Regents
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
import boa.datagen.forges.github.LocalGitSequenceGenerator;
import boa.datagen.forges.github.MetaDataMaster;

/**
 * The main entry point for Boa tools for generating datasets.
 *
 * @author hridesh
 * @author rdyer
 */
public class BoaGenerator {
	private static boolean jsonAvailable = true;
	private static boolean tokenAvailable = false;

	public static void main(final String[] args) throws IOException {
		final Options options = new Options();
		BoaGenerator.addOptions(options);

		final CommandLine cl;
		try {
			cl = new PosixParser().parse(options, args);
		} catch (final org.apache.commons.cli.ParseException e) {
			System.err.println(e.getMessage());
			printHelp(options);
			return;
		}
		BoaGenerator.handleCmdOptions(cl, options, args);
		if (cl.hasOption("help"))
			return;

		if (cl.hasOption("recover")) {
			SeqCombiner.main(new String[0]);
		} else {
			/*
			 * 1. if user provides local JSON files
			 * 2. if user provides username and password
			 *
			 * in both cases, JSON files are going to be available
			 */

			if (jsonAvailable) {
				try {
					SeqRepoImporter.main(new String[0]);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				if (!cl.hasOption("nocombine"))
					SeqCombiner.main(new String[0]);
			} else if (tokenAvailable) { // when user provides local repo and doesn't have json files
				final MetaDataMaster mdm = new MetaDataMaster();
				mdm.downloadRepoNames(DefaultProperties.TOKEN, DefaultProperties.OUTPUT);

				if (!cl.hasOption("nocombine"))
					SeqCombiner.main(new String[0]);
			} else { // when user provides local repo and does not have json files
				final File output = new File(DefaultProperties.OUTPUT);
				if (!output.exists())
					output.mkdirs();
				LocalGitSequenceGenerator.localGitSequenceGenerate(DefaultProperties.GH_GIT_PATH, DefaultProperties.OUTPUT);
				try {
					MapFileGen.main(new String[0]);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}

			clear();
		}
	}

	private static final void printHelp(final Options options) {
		final String header = "Boa dataset generation options:";
		final String footer = "\nPlease report issues at https://github.com/boalang/compiler/";
		new HelpFormatter().printHelp("boa -g [options]", header, options, footer);
	}

	private static void addOptions(final Options options) {
		options.addOption("inputJson", true, ".json files for metadata");
		options.addOption("inputToken", true, "token file");
		options.addOption("inputRepo", true, "cloned repo path");
		options.addOption("threads", true, "number of threads");
		options.addOption("projects", true, "maximum number of projects per sequence file");
		options.addOption("maxprojects", true, "total maximum number of projects");
		options.addOption("commits", true, "maximum number of commits of a project to be stored in the project object");
		options.addOption("nocommits", false, "do not store commits");
		options.addOption("size", true, "maximum size of a project object to be stored");
		options.addOption("libs", true, "directory to store libraries");
		options.addOption("output", true, "directory where output is stored");
		options.addOption("combineoutput", true, "directory where combiner output is stored");
		options.addOption("user", true, "github username to authenticate");
		options.addOption("password", true, "github password to authenticate");
		options.addOption("targetUser", true, "username of target repository");
		options.addOption("targetRepo", true, "name of the target repository");
		options.addOption("cache", false, "enable if you want to use already cloned repositories");
		options.addOption("skip", true, "skip N projects after each processed project (useful for sampling)");
		options.addOption("offset", true, "the offset for the first project to process");
		options.addOption("recover", false, "enable to recover partially built dataset - this will only combine generated data");
		options.addOption("nocombine", false, "do not combine generated seq files into final form");
		options.addOption("debug", false, "enable for debug mode");
		options.addOption("debugparse", false, "enable for debug mode when parsing source files");
		options.addOption("help", false, "shows this help");
	}

	private static void handleCmdOptions(final CommandLine cl, final Options options, final String[] args) {
		if (cl.hasOption("inputJson") && cl.hasOption("inputRepo") && cl.hasOption("output")) {
			DefaultProperties.GH_JSON_PATH = cl.getOptionValue("inputJson");
			DefaultProperties.OUTPUT = cl.getOptionValue("output");
			// DefaultProperties.GH_GIT_PATH = GH_JSON_CACHE_PATH + "/github";
			DefaultProperties.GH_GIT_PATH = cl.getOptionValue("inputRepo");
		} else if (cl.hasOption("inputJson") && cl.hasOption("output")) {
			DefaultProperties.GH_JSON_PATH = cl.getOptionValue("inputJson");
			DefaultProperties.OUTPUT = cl.getOptionValue("output");
			DefaultProperties.GH_GIT_PATH = cl.getOptionValue("output");
		} else if (cl.hasOption("inputToken") && cl.hasOption("inputRepo") && cl.hasOption("output")) {
			DefaultProperties.TOKEN = cl.getOptionValue("inputToken");
			DefaultProperties.OUTPUT = cl.getOptionValue("output");
			// DefaultProperties.GH_GIT_PATH = GH_JSON_CACHE_PATH + "/github";
			DefaultProperties.GH_GIT_PATH = cl.getOptionValue("inputRepo");
			jsonAvailable = false;
			tokenAvailable = true;
		} else if (cl.hasOption("inputRepo") && cl.hasOption("output")) {
			DefaultProperties.OUTPUT = cl.getOptionValue("output");
			DefaultProperties.GH_GIT_PATH = cl.getOptionValue("inputRepo");
			jsonAvailable = false;
		} else if (cl.hasOption("user") && cl.hasOption("password") && cl.hasOption("targetUser")
				&& cl.hasOption("targetRepo") && cl.hasOption("output")) {
			try {
				// because there is no input directory in this case, we need to
				// create one
				final String GH_JSON_PATH = new java.io.File(".").getCanonicalPath();
				DefaultProperties.GH_JSON_PATH = GH_JSON_PATH + "/input";
				getGithubMetadata(DefaultProperties.GH_JSON_PATH, cl.getOptionValue("user"),
						cl.getOptionValue("password"), cl.getOptionValue("targetUser"),
						cl.getOptionValue("targetRepo"));

				// output directory
				final String GH_JSON_CACHE_PATH = cl.getOptionValue("output");
				DefaultProperties.OUTPUT = GH_JSON_CACHE_PATH;
				DefaultProperties.GH_GIT_PATH = GH_JSON_CACHE_PATH + "/github";
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (cl.hasOption("recover") && cl.hasOption("output")) {
			DefaultProperties.OUTPUT = cl.getOptionValue("output");
		} else {
			if (!cl.hasOption("help"))
				System.err.println("Must specify the output, and the local input paths (JSON and repository) or remote login information.");
			printHelp(options);
			System.exit(1);
		}
		if (cl.hasOption("threads")) {
			DefaultProperties.NUM_THREADS = cl.getOptionValue("threads");
		}
		if (cl.hasOption("combineoutput")) {
			DefaultProperties.COMBINER_OUTPUT = cl.getOptionValue("combineoutput");
		} else {
			DefaultProperties.COMBINER_OUTPUT = DefaultProperties.OUTPUT;
		}
		if (cl.hasOption("projects")) {
			DefaultProperties.MAX_PROJECTS = cl.getOptionValue("projects");
		}
		if (cl.hasOption("commits")) {
			DefaultProperties.MAX_COMMITS = cl.getOptionValue("commits");
		}
		if (cl.hasOption("size")) {
			DefaultProperties.MAX_SIZE_FOR_PROJECT_WITH_COMMITS = cl.getOptionValue("size");
		}
		if (cl.hasOption("maxprojects")) {
			DefaultProperties.TOTAL_MAX_PROJECTS = cl.getOptionValue("maxprojects");
		}
		if (cl.hasOption("debug")) {
			DefaultProperties.DEBUG = true;
		}
		if (cl.hasOption("debugparse")) {
			DefaultProperties.DEBUGPARSE = true;
		}
		if (cl.hasOption("cache")) {
			DefaultProperties.CACHE = true;
		}
		if (cl.hasOption("skip")) {
			DefaultProperties.SKIPS = cl.getOptionValue("skip");
		}
		if (cl.hasOption("offset")) {
			DefaultProperties.OFFSET = cl.getOptionValue("offset");
		}
		if (cl.hasOption("libs")) {
			DefaultProperties.CLASSPATH_ROOT = cl.getOptionValue("libs");
		}
		if (cl.hasOption("nocommits"))
			DefaultProperties.STORE_COMMITS = false;
	}

	//
	private static void clear() {
		final File inputDirectory = new File(DefaultProperties.OUTPUT + "/buf-map");
		if (inputDirectory.exists())
			org.apache.commons.io.FileUtils.deleteQuietly(inputDirectory);
	}

	private static void getGithubMetadata(final String inputPath, final String username, final String password, final String targetUser,
			final String targetRepo) {
		final String[] args = { inputPath, username, password, targetUser, targetRepo };
		GetGithubRepoByUser.main(args);
	}
}
