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
package boa.evaluator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;

import boa.BoaMain;
import boa.compiler.BoaCompiler;
import boa.datagen.DefaultProperties;
import boa.datagen.util.FileIO;

/**
 * The main entry point for Boa REPL.
 *
 * @author hridesh
 * @author rdyer
 */
public class BoaEvaluator extends BoaMain {
	private final String PROG_PATH;
	private final String DATA_PATH;
	private final String COMPILATION_DIR;
	private final String OUTPUT_DIR;

	public BoaEvaluator(final String prog, final String data) throws IOException {
		this(prog, data, System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString());
	}

	public BoaEvaluator(final String prog, final String data, final String outDir) throws IOException {
		this.PROG_PATH = prog;
		this.DATA_PATH = data;
		this.COMPILATION_DIR = "./compile"; // can not customize to be user defined because of classpath issues
		this.OUTPUT_DIR = outDir;

		setup();
	}

	public static void main(final String[] args) {
		final Options options = new Options();

		options.addOption("i", "input",  true, "input Boa source file (*.boa)");
		options.addOption("d", "data",   true, "path to local data directory");
		options.addOption("o", "output", true, "output directory");

		options.getOption("i").setRequired(true);
		options.getOption("d").setRequired(true);

		try {
			if (args.length == 0) {
				printHelp(options, null);
				return;
			} else {
				final CommandLine cl = new PosixParser().parse(options, args);

				if (cl.hasOption('i') && cl.hasOption('d')) {
					final BoaEvaluator evaluator;
					try {
						if (cl.hasOption('o')) {
							evaluator = new BoaEvaluator(cl.getOptionValue('i'), cl.getOptionValue('d'), cl.getOptionValue('o'));
						} else {
							evaluator = new BoaEvaluator(cl.getOptionValue('i'), cl.getOptionValue('d'));
						}
					} catch (final IOException e) {
						System.err.print(e);
						return;
					}

					if (!evaluator.compile()) {
						System.err.println("Compilation Failed");
						return;
					}

					final long start = System.currentTimeMillis();
					evaluator.evaluate();
					final long end = System.currentTimeMillis();

					System.out.println("Total Time Taken: "+ (end - start));
					System.out.println(evaluator.getResults());
				} else {
					printHelp(options, "missing required options: -i <arg> and -d <arg>");
					return;
				}
			}
		} catch (final org.apache.commons.cli.ParseException e) {
			printHelp(options, e.getMessage());
		}
	}

	public void evaluate() {
		final String[] actualArgs = createHadoopProgramArguments();
		final File srcDir = new File(this.COMPILATION_DIR);

		try {
			final URL srcDirUrl = srcDir.toURI().toURL();

			final ClassLoader cl = new URLClassLoader(new URL[] { srcDirUrl }, ClassLoader.getSystemClassLoader());
			final Class<?> cls = cl.loadClass("boa." + jarToClassname(this.PROG_PATH));
			final Method method = cls.getMethod("main", String[].class);

			method.invoke(null, (Object)actualArgs);
		} catch (final Throwable e) {
			System.err.print(e);
		}
	}

	public String getResults() {
		for (final File f : new File(this.OUTPUT_DIR).listFiles()) {
			if (f.getName().startsWith("part")) {
				return FileIO.readFileContents(f);
			}
		}

		return "";
	}

	private boolean compile() {
		try{
			BoaCompiler.main(createCompilerArguments());
		} catch (final Exception e) {
			System.err.print(e);
			return false;
		}

		return true;
	}

	private String[] createCompilerArguments() {
		final String[] compilationArgs = new String[6];

		compilationArgs[0] = "-i";
		compilationArgs[1] = this.PROG_PATH;
		compilationArgs[2] = "-j";
		compilationArgs[3] = "./dist/boa-runtime.jar";
		compilationArgs[4] = "-cd";
		compilationArgs[5] = this.COMPILATION_DIR;

		return compilationArgs;
	}

	private String[] createHadoopProgramArguments() {
		final String[] actualArgs = new String[3];

		actualArgs[0] = this.DATA_PATH;
		actualArgs[1] = this.OUTPUT_DIR;
		actualArgs[2] = "-b"; // blocking call

		return actualArgs;
	}

	/**
	 * Performs some initial setup steps like
	 * 1. makes sure that output directory does not already exist
	 * 2. makes sure that compilation directory exists
	 */
	private void setup() throws IOException {
		final File compilationDir = new File(this.COMPILATION_DIR);

		// output directory already exists, remove it
		if (this.OUTPUT_DIR != null) {
			final File outputDir = new File(this.OUTPUT_DIR);

			if (outputDir.exists()) {
				System.err.print("output directory '" + this.OUTPUT_DIR + "' exists - delete? [Y/n] ");
				int b = (char)System.in.read();
				char ch = (char)b;

				if (b == -1 || ch == 'Y' || ch == 'y' || ch == '\n' || ch == '\r') {
					FileUtils.deleteDirectory(outputDir);
				} else {
					throw new RuntimeException("Please remove or provide a different output directory.");
				}
			}
		}

		// compilation directory already exists, remove it
		if (compilationDir.exists()) {
			FileUtils.deleteDirectory(compilationDir);
		}

		// compilation directory must exist
		// for now only absolute paths are supported
		//TODO: support for relative path directory support
		compilationDir.mkdirs();

		// set localData path in Defaultproperties
		DefaultProperties.localDataPath = this.DATA_PATH;
	}
}
