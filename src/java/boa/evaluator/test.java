package boa.evaluator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import static boa.compiler.BoaCompiler.*;

public class test {
	
	static String program = "/Users/hyj/boa-workspace/refactoring/code_change/histrees.boa";
	static String compile = "./compile";
	static String jarName = "Histrees.jar";
	static String outputRoot = "./compile";
	static String outputFile = "./compile/boa/Histrees.java";

	public static void main(String[] args) throws IOException {
		
//		BasicConfigurator.configure();
		PropertyConfigurator.configure("/Users/hyj/boa-workspace/compiler/log4j.properties");
		
		final String[] actualArgs = {"/Users/hyj/git/BoaData/DataSet/aa_toy", "/Users/hyj/git/BoaData/QueryOutput", "-b"};
		final File srcDir = new File("./compile");
		String output = "/Users/hyj/git/BoaData/QueryOutput";
		FileUtils.deleteDirectory(new File(output));
		
//		CommandLine clc = processCommandLineOptions(createCompilerArguments());
//		// compile
//		compileGeneratedSrc(clc, jarName, new File(outputRoot), new File(outputFile));
//		
		
		URLClassLoader cl = null;
		try {
			final URL srcDirUrl = srcDir.toURI().toURL();

			cl = new URLClassLoader(new URL[] { srcDirUrl }, ClassLoader.getSystemClassLoader());
			final Class<?> cls = cl.loadClass("boa.Histrees");
			final Method method = cls.getMethod("main", String[].class);

			method.invoke(null, (Object)actualArgs);
		} catch (final Throwable e) {
			System.err.print(e.getCause());
		} finally {
			if (cl != null)
				try {
					cl.close();
				} catch (final IOException e) { }
		}
		
		
		
	}
	
	private static String[] createCompilerArguments() {
		final String[] compilationArgs = new String[6];

		compilationArgs[0] = "-i";
		compilationArgs[1] = program;
		compilationArgs[2] = "-j";
		compilationArgs[3] = "./dist/boa-runtime.jar";
		compilationArgs[4] = "-cd";
		compilationArgs[5] = compile;

		return compilationArgs;
	}
}
