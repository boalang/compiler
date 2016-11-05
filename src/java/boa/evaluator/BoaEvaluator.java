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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.UUID;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import com.google.protobuf.GeneratedMessage;

//import boa.Example;
import boa.compiler.BoaCompiler;
import boa.datascience.evaluationEngine.AbstractEvaluationEngine;
import boa.datascience.externalDataSources.DatagenProperties;

/**
 * The main entry point for Boa REPL.
 *
 * @author hridesh
 * 
 */
public class BoaEvaluator extends AbstractEvaluationEngine {

	public BoaEvaluator(String prog, String data) {
		super(prog, data);
		// TODO Auto-generated constructor stub
	}

	public BoaEvaluator(String prog, String data, String outputPath) {
		super(prog, data);
		DatagenProperties.BOA_OUTPUT_DIR_PATH = outputPath;
	}

	private static final void printHelp(Options options, String message) {
		String header = "The most commonly used Boa options are:";
		String footer = "\nPlease report issues at http://www.github.com/boalang/";
		System.err.println(message);
		new HelpFormatter().printHelp("Boa", header, options, footer);
	}

	@Override
	public boolean evaluate() {
		String[] compilationArgs = new String[6];
		final String compilationRoot = new File(new File(System.getProperty("java.io.tmpdir")),
				UUID.randomUUID().toString()).getAbsolutePath();
		compilationArgs[0] = "-i";
		compilationArgs[1] = this.inputProgram;
		compilationArgs[2] = "-j";
		compilationArgs[3] = "./dist/boa-runtime.jar";
		compilationArgs[4] = "-gcd";
		compilationArgs[5] = compilationRoot;

		try {
			BoaCompiler.main(compilationArgs);
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		String[] actualArgs = new String[2];
		String genFileName = getClassNameForGeneratedJavaProg();
		String genClassName = "boa." + genFileName;
		genClassName.replace("/", ".");
		actualArgs[0] = this.inputData;
		actualArgs[1] = DatagenProperties.BOA_OUT;
		while (genClassName.startsWith(".")) {
			genClassName = genClassName.substring(1);
		}
		String file = compilationRoot + "/";
		try {
			URL url = new File(file).toURI().toURL();
			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { url });
			Class<?> prog = Class.forName(genClassName, true, classLoader);
			Method main = prog.getMethod("main", String[].class);
			Object[] arguments = { actualArgs };
			main.invoke(null, arguments);
			System.out.println("Output:" + DatagenProperties.BOA_OUT);
		} catch (IOException | ClassNotFoundException | NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
			e1.printStackTrace();
		}
		System.out.println("Output:" + DatagenProperties.BOA_OUT);
		return true;
	}

	@Override
	public List<GeneratedMessage> getData() {
		return null;
	}

	public static void main(final String[] args) {
		if (args.length != 2) {
			throw new IllegalArgumentException();
		}
		String program = args[0];
		String data = args[1];
		BoaEvaluator evaluator = new BoaEvaluator(program, data);
		evaluator.evaluate();
	}

	private String getClassNameForGeneratedJavaProg() {
		String name = this.inputProgram;
		if (name.contains("/")) {
			name = name.substring(name.lastIndexOf('/') + 1);
		}
		if (name.contains(".")) {
			name = name.substring(0, name.lastIndexOf('.'));
		}
		name = name.substring(0, 1).toUpperCase() + name.substring(1);
		return name;
	}
}
