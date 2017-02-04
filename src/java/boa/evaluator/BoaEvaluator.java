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
package boa.evaluator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;

import boa.compiler.BoaCompiler;
import boa.datagen.DefaultProperties;
import boa.datagen.util.FileIO;

/**
 * The main entry point for Boa REPL.
 *
 * @author hridesh
 *
 */
public class BoaEvaluator {

	private final String PROG_PATH;
	private final String DATA_PATH;
	private final String COMPILATION_DIR;
	private  String OUTPUT_DIR;

	public BoaEvaluator(String prog, String data) {
		this.PROG_PATH = prog;
		this.DATA_PATH = data;
		this.COMPILATION_DIR = "./compile"; // can not customize to be user defined because of classpath issues
		this.OUTPUT_DIR =  System.getProperty("java.io.tmpdir");
		setup();
	}

	public BoaEvaluator(String prog, String data, String outDir){
		this(prog, data);
		this.OUTPUT_DIR = outDir;
		setup();
	}

	private static final void printHelp(Options options, String message) {
		String header = "The most commonly used Boa options are:";
		String footer = "\nPlease report issues at http://www.github.com/boalang/";
		System.err.println(message);
		new HelpFormatter().printHelp("Boa", header, options, footer);
	}

	public void evaluate() {
		final String[] actualArgs = createHadoopProgramArguments();
		File srcDir = new File(this.COMPILATION_DIR);
		try {
			URL srcDirUrl = srcDir.toURI().toURL();

			ClassLoader cl = new URLClassLoader(new URL[] { srcDirUrl }, ClassLoader.getSystemClassLoader());
			Class<?> cls = cl.loadClass(getClassNameForGeneratedJavaProg());
			final Method method = cls.getMethod("main", String[].class);
			method.invoke(null, (Object) actualArgs);

		} catch (Throwable exc) {
			exc.printStackTrace();
		}
	}

	public static void main(final String[] args) {
		final Options options = new Options();
		options.addOption("i", "input",  true, "input Boa program");
		options.addOption("d", "data", true, "path to local data");
		options.addOption("o", "out", true, "output directory");

		final CommandLine cl;
		try{
			if(args.length == 0) {
				printHelp(options, "");
				return;
			} else {
				cl = new PosixParser().parse(options, args);
				if (cl.hasOption('i') && cl.hasOption('d')) {
					BoaEvaluator evaluator;
					if(cl.hasOption('o')) {
						evaluator = new BoaEvaluator(cl.getOptionValue('i'), cl.getOptionValue('d'), cl.getOptionValue('o'));
					} else{
						evaluator = new BoaEvaluator(cl.getOptionValue('i'), cl.getOptionValue('d'));
					}
					long start = System.currentTimeMillis();
					if(!evaluator.compile()) {
						System.err.println("Compilation Failed");
						return;
					}
					evaluator.evaluate();
					long end = System.currentTimeMillis();
					System.out.println("Total Time Taken: "+ (end - start));
					System.out.println(evaluator.getResults());
				}else {
					printHelp(options, "");
					return;
				}
			}
		} catch (final org.apache.commons.cli.ParseException e) {
			printHelp(options, e.getMessage());
		}

	}

	private String getClassNameForGeneratedJavaProg() {
		StringBuffer progName = new StringBuffer(this.PROG_PATH);
		while (progName.charAt(0) == '.' || progName.charAt(0) == '/') {
			progName.deleteCharAt(0);
		}
		progName.delete(progName.lastIndexOf(".boa"), progName.length());
		progName.delete(0, progName.lastIndexOf("/") + 1);
		progName.setCharAt(0, Character.toUpperCase(progName.charAt(0)));
		progName.insert(0, "boa.");
		progName = new StringBuffer(progName.toString().replace("/", "."));
		return progName.toString();
	}

	public String getResults() {
		for (File f : new File(this.OUTPUT_DIR).listFiles()) {
			if (f.getName().startsWith("part")) {
				return FileIO.readFileContents(f);
			}
		}
		return "";
	}

	private boolean compile() {
		final String[] compilationArgs = createCompilerArguments();
		try{
			BoaCompiler.main(compilationArgs);
		}catch (Exception e) {
			e.printStackTrace();
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
	 * 1. makes sure that output sirectory does not already exists
	 * 2. makes sure that compilation directory does exists
	 */
	private void setup() {
		File compilationDir =  new File(this.COMPILATION_DIR);
		// output directory does not already exists
		try {
			if(this.OUTPUT_DIR != null) {
				FileUtils.deleteDirectory(new File(this.OUTPUT_DIR));
			}
			FileUtils.deleteDirectory(compilationDir);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// compilation directory must exist
		// for now only absolute paths are supported
		//TODO: support for relative path directory support
		compilationDir.mkdirs();

		// set localData path in Defaultproperties
		DefaultProperties.localDataPath = this.DATA_PATH;
	}
}