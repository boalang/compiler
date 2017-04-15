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
package boa.datagen;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import boa.BoaMain;
import boa.datagen.forges.github.GetGithubRepoByUser;
import boa.datagen.forges.github.LocalGitSequenceGenerator;

/**
 * The main entry point for Boa tools for generating datasets.
 *
 * @author hridesh
 * @author rdyer
 */
public class BoaGenerator extends BoaMain {
	public static boolean jsonAvailable = true;
	public static boolean localCloning = false;

	public static void main(final String[] args) throws IOException {
		final Options options = new Options();
		BoaGenerator.addOptions(options);

		final CommandLine cl;
		try {
			cl = new PosixParser().parse(options, args);
			BoaGenerator.handleCmdOptions(cl, options, args);
		} catch (final Exception e) {
			printHelp(options, e.getMessage());
			return;
		}

		/*
		 * 1. if user provides local json files 2. if user provides username and
		 * password in both the cases json files are going to be available
		 */

		if (jsonAvailable) {
			CacheGithubJSON.main(args);
			try {
				SeqRepoImporter.main(args);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}

			// SeqProjectCombiner.main(args);
			// SeqSort.main(args);
			// SeqSortMerge.main(args);
			try {
				MapFileGen.main(args);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		} else { // when user provides local repo and does not have json files
			final File output = new File(DefaultProperties.GH_JSON_CACHE_PATH);
			if (!output.exists())
				output.mkdirs();
				LocalGitSequenceGenerator.localGitSequenceGenerate(DefaultProperties.GH_GIT_PATH, DefaultProperties.GH_JSON_CACHE_PATH);
			try {
				MapFileGen.main(args);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		if (cl.hasOption("cache"))
			clear(true);
		else
			clear(false);
	}

	private static void addOptions(Options options) {
		options.addOption("ij", "inputJson", true, ".json files for metadata");
		options.addOption("ir", "inputRepo", true, "cloned repo path");
		options.addOption("o", "output", true, "directory where output is desired");
		options.addOption("u", "user", true, "github username to authenticate");
		options.addOption("p", "password", true, "github password to authenticate.");
		options.addOption("tu", "targetUser", true, "username of target repository");
		options.addOption("tr", "targetRepo", true, "name of the target repository");
		options.addOption("c", "cache", false, "enable if you want to delete the cloned code for user.");
	}

	private static void handleCmdOptions(CommandLine cl, Options options, final String[] args) throws IOException {
		if (cl.hasOption("inputJson") && cl.hasOption("inputRepo") && cl.hasOption("output")) {
			DefaultProperties.GH_JSON_PATH = cl.getOptionValue("inputJson");
			DefaultProperties.GH_JSON_CACHE_PATH = cl.getOptionValue("output");
			// DefaultProperties.GH_GIT_PATH = GH_JSON_CACHE_PATH + "/github";
			DefaultProperties.GH_GIT_PATH = cl.getOptionValue("inputRepo");
			localCloning = true;
		} else if (cl.hasOption("inputJson") && cl.hasOption("output")) {
			DefaultProperties.GH_JSON_PATH = cl.getOptionValue("inputJson");
			DefaultProperties.GH_JSON_CACHE_PATH = cl.getOptionValue("output");
			DefaultProperties.GH_GIT_PATH = cl.getOptionValue("output");
		} else if (cl.hasOption("inputRepo") && cl.hasOption("output")) {
			DefaultProperties.GH_JSON_CACHE_PATH = cl.getOptionValue("output");
			DefaultProperties.GH_GIT_PATH = cl.getOptionValue("inputRepo");
			jsonAvailable = false;
			localCloning = true;
		} else if (cl.hasOption("user") && cl.hasOption("password") && cl.hasOption("targetUser")
				&& cl.hasOption("targetRepo") && cl.hasOption("output")) {
			// because there is no input directory in this case, we need to
			// create one
			final String GH_JSON_PATH = new java.io.File(".").getCanonicalPath();
			DefaultProperties.GH_JSON_PATH = GH_JSON_PATH + "/input";
			getGithubMetadata(DefaultProperties.GH_JSON_PATH, cl.getOptionValue("user"),
					cl.getOptionValue("password"), cl.getOptionValue("targetUser"),
					cl.getOptionValue("targetRepo"));

			// output directory
			final String GH_JSON_CACHE_PATH = cl.getOptionValue("output");
			DefaultProperties.GH_JSON_CACHE_PATH = GH_JSON_CACHE_PATH;
			DefaultProperties.GH_GIT_PATH = GH_JSON_CACHE_PATH + "/github";
		} else {
			throw new RuntimeException("User must specify the path of the repository. Please see --remote and --local options");
		}
	}

	private static void clear(final boolean cache) {
		if (!cache) {
			final File clonedCode = new File(DefaultProperties.GH_GIT_PATH);
			if (clonedCode.exists())
				org.apache.commons.io.FileUtils.deleteQuietly(clonedCode);
		}
		final File inputDirectory = new File(DefaultProperties.GH_JSON_CACHE_PATH + "/buf-map");
		if (inputDirectory.exists())
			org.apache.commons.io.FileUtils.deleteQuietly(inputDirectory);
	}

	private static void getGithubMetadata(final String inputPath, final String username, final String password, final String targetUser, final String targetRepo) {
		GetGithubRepoByUser.main(new String[] { inputPath, username, password, targetUser, targetRepo });
	}
}
