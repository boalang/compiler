/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
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
 * @author rdyer
 */
@RunWith(Parameterized.class)
public class TestInhAttr extends BaseTest {
	final private static String rootDir = "test/inhattr/";

	@Parameters(name = "{0}")
	public static List<String[]> data() {
		final List<String[]> files = new ArrayList<String[]>();
		for (final File f : new File(rootDir).listFiles())
			if (!f.isDirectory() && f.getName().endsWith(".boa"))
				files.add(new String[] { f.getPath(), null });
		return files;
	}

	private String fileName;
	public TestInhAttr(final String fileName, final String ignored) {
		this.fileName = fileName;
	}


	// test a bunch of known good files
	@Test
	public void knownGood() throws IOException {
		codegen(load(fileName));
	}
}
