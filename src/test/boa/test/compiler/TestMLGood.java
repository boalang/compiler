package boa.test.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author hyj
 */
@RunWith(Parameterized.class)
public class TestMLGood extends BaseTest {
	final private static String rootDir = "test/ml/";
	
	@Parameters(name = "{0}")
	public static List<String[]> data() {
		final List<String[]> files = new ArrayList<String[]>();
		addTestFiles(files, new File(rootDir));
		return files;
	}
	
	private static void addTestFiles(List<String[]> files, File file) {
		for (final File f : file.listFiles()) {
			if (f.isDirectory())
				addTestFiles(files, f);
			else if (f.getName().endsWith(".boa"))
				files.add(new String[] { f.getPath(), null });
		}
	}

	private String fileName;
	public TestMLGood(final String fileName, final String ignored) {
		this.fileName = fileName;
	}


	// test a bunch of known good files
	@Test
	public void knownGood() throws IOException {
		codegen(load(fileName));
	}
}
