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
package boa.test.datagen.kotlin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import boa.datagen.util.FileIO;
import boa.datagen.util.KotlinErrorCheckVisitor;
import boa.functions.langmode.KotlinLangMode;

/**
 * @author rdyer
 */
@RunWith(Parameterized.class)
public class SaveKotlinTestResult extends KotlinBaseTest {
	final private static String rootDir = "test/datagen/kotlin";

	@Parameters(name = "{0}")
	public static List<String[]> data() {
		return getData(new File(rootDir));
	}

	private static List<String[]> getData(final File root) {
		final List<String[]> files = new ArrayList<String[]>();
		for (final File f : root.listFiles())
			if (f.isDirectory()) {
				files.addAll(getData(f));
			} else if (f.getName().endsWith(".kt")) {
				final File f2 = new File(f.getPath().replace(".kt", ".json"));
				if (f2.exists() && !f2.isDirectory())
					files.add(new String[] { f.getPath(), f2.getPath()});
			}
		return files;
	}

	private String kotlinFileName;
	private String jsonFileName;
	private String actualJsonFileName;
	public SaveKotlinTestResult(final String kotlinFileName, final String jsonFileName) {
		this.kotlinFileName = kotlinFileName;
		this.actualJsonFileName = jsonFileName;
		this.jsonFileName = jsonFileName + "2";
	}

	private final KotlinErrorCheckVisitor errorCheck = new KotlinErrorCheckVisitor();

	@Test
	public void saveKotlinResult() throws IOException {
		final String src = load(kotlinFileName);
		final String content = parseKotlin(src).trim();
		if (errorCheck.hasError(KotlinLangMode.tryparse("test.kt", src, false))) {
			boa.datagen.util.FileIO.writeFileContents(new File(jsonFileName + ".error"), content, false);
		} else {
			FileIO.delete(new File(jsonFileName));
			if (!content.equals(load(actualJsonFileName).trim()))
				boa.datagen.util.FileIO.writeFileContents(new File(jsonFileName), content, false);
		}
	}
}
