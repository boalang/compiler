package boa.test.datagen.queries;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import boa.datagen.util.FileIO;
import boa.evaluator.BoaEvaluator;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;


public abstract class QueryTest {
	
	@Before
	public void prep() {
		File outputDir = new File("test/datagen/temp_output");
		if (outputDir.exists()) {
			try {
				FileUtils.deleteDirectory(outputDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getResults(File outputDir) {
		for (final File f : outputDir.listFiles()) {
			if (f.getName().startsWith("part")) {
				return FileIO.readFileContents(f);
			}
		}
		return "";
	}

	public void queryTest(String inputPath, String expected) {
		String[] args = { "-i", inputPath, "-d", "test/datagen/test_datagen", "-o", "test/datagen/temp_output" };
		BoaEvaluator.main(args);
		File outputDir = new File("test/datagen/temp_output");
		String actual = getResults(outputDir);
		try {
			FileUtils.deleteDirectory(outputDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(expected, actual);
	}
}
