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
package boa.test.datagen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
public class TestKotlin extends KotlinBaseTest {
	final private static String rootDir = "test/datagen/kotlin";

	@Parameters(name = "{0}")
	public static List<String[]> data() {
		final List<String[]> files = new ArrayList<String[]>();
		for (final File f : new File(rootDir).listFiles())
			if (!f.isDirectory() && f.getName().endsWith(".kt")) {
				final File f2 = new File(f.getPath().replace(".kt", ".json"));
				if (!f2.exists() && f2.isDirectory())
					files.add(new String[] { f.getPath(), f2.getPath()});
			}
		return files;
	}

	private String kotlinFileName;
	private String jsonFileName;
	public TestKotlin(final String kotlinFileName, final String jsonFileName) {
		this.kotlinFileName = kotlinFileName;
		this.jsonFileName = jsonFileName;
	}


	// test a bunch of known good files
	@Test
	public void kotlin() throws IOException {
		assertEquals(
			parseKotlin(load(kotlinFileName)).trim(),
			load(jsonFileName).trim()
		);
	}
}
