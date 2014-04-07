package boa.test.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestGood extends BaseTest {
	final private static String goodDir = "test/known-good/";

	@Parameters(name = "{0}")
	public static List<String[]> data() {
		final List<String[]> files = new ArrayList<String[]>();
		for (final File f : new File(goodDir).listFiles())
			if (!f.isDirectory() && f.getName().endsWith(".boa"))
				files.add(new String[] { f.getPath(), null });
		return files;
	}

	private String fileName;
	public TestGood(final String fileName, final String ignored) {
		this.fileName = fileName;
	}


	// test a bunch of known good files
	@Test
	public void knownGood() throws IOException {
		codegen(load(fileName));
	}
}
