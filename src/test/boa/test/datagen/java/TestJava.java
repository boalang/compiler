/*
 * Copyright 2021, Robert Dyer
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
package boa.test.datagen.java;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author rdyer
 */
@RunWith(Parameterized.class)
public class TestJava extends Java8BaseTest {
	final private static String rootDir = "test/datagen/java";
	final private static String rootDirWrapped = "test/datagen/java-wrapped";

	public static class FileTestCaseComparator implements Comparator<String[]> {
		@Override
		public int compare(final String[] o1, final String[] o2) {
			return o1[0].compareTo(o2[0]);
		}
	}

	@Parameters(name = "{0}")
	public static List<String[]> data() {
		final List<String[]> data = getData(new File(rootDir), false);
		data.addAll(getData(new File(rootDirWrapped), true));
		Collections.sort(data, new FileTestCaseComparator());
		return data;
	}

	private static List<String[]> getData(final File root, final boolean isWrapped) {
		if (!root.isDirectory()) return new ArrayList<String[]>();
		final List<String[]> files = new ArrayList<String[]>();
		for (final File f : root.listFiles())
			if (f.isDirectory()) {
				files.addAll(getData(f, isWrapped));
			} else if (f.getName().endsWith(".java")) {
				final File f2 = new File(f.getPath().replace(".java", ".json"));
				if (f2.exists() && !f2.isDirectory())
					files.add(new String[] { f.getPath(), f2.getPath(), isWrapped ? null : "" });
			}
		return files;
	}

	private String javaFileName;
	private String jsonFileName;
	private boolean isWrapped;
	public TestJava(final String javaFileName, final String jsonFileName, final String isWrapped) {
		this.javaFileName = javaFileName;
		this.jsonFileName = jsonFileName;
		this.isWrapped = isWrapped == null;
	}


	// test a bunch of known good files
	@Test
	public void java() throws IOException {
		if (isWrapped)
			testWrapped(
				load(javaFileName).trim(),
				load(jsonFileName).trim()
			);
		else
			assertEquals(
				load(jsonFileName).trim(),
				parseJava(load(javaFileName)).trim()
			);
	}
}
