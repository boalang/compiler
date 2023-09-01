/*
 * Copyright 2022, Robert Dyer,
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
package boa.test.datagen.queries;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.Test;

import boa.datagen.util.FileIO;
import boa.evaluator.BoaEvaluator;


/**
 * @author rdyer
 */
@RunWith(Parameterized.class)
public class TestQueries {
	final private static String queryDir    = "test/known-good";
	final private static String expectedDir = "test/datagen/expected";

	final private static String datasetPath = "test/datagen/test_datagen";
	final private static String outPath     = "test/datagen/temp_output";

	@Parameters(name = "{0}")
	public static List<String[]> data() {
		final List<String[]> files = new ArrayList<String[]>();
		for (final File f : new File(queryDir).listFiles())
			if (!f.isDirectory() && f.getName().endsWith(".boa")) {
				final File f2 = new File(expectedDir, f.getPath() + ".txt");
				if (f2.exists())
					files.add(new String[] { f.getPath(), f2.getPath() });
			}
		return files;
	}

	private String queryName;
	private String expectedName;
	public TestQueries(final String queryName, final String expectedName) {
		this.queryName = queryName;
		this.expectedName = expectedName;
	}

	@Test
	public void query() {
		BoaEvaluator.main(new String[] {
			"-i", queryName,
			"-d", datasetPath,
			"-o", outPath
		});

		assertEquals(FileIO.readFileContents(new File(expectedName)), getResults());
	}

	@Before
	@After
	public void clearOutput() {
		final File outputDir = new File(outPath);
		if (outputDir.exists()) {
			try {
				FileUtils.deleteDirectory(outputDir);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected String getResults() {
		for (final File f : new File(outPath).listFiles())
			if (f.getName().startsWith("part-r-"))
				return FileIO.readFileContents(f);
		return "";
	}
}
