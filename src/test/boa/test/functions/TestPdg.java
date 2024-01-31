/*
 * Copyright 2018, Robert Dyer,
 *                 and Bowling Green State University
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
package boa.test.functions;

import static org.junit.Assert.assertEquals;
import static boa.functions.BoaAstIntrinsics.parse;
import static boa.functions.BoaGraphIntrinsics.getpdg;
import static boa.functions.BoaGraphIntrinsics.pdgToDot;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Ast.Method;

/**
 * Test PDG generation.
 *
 * @author rdyer
 */
@RunWith(Parameterized.class)
public class TestPdg {
	final private static File srcRootDir = new File("test/pdg/src");
	final private static File dotRootDir = new File("test/pdg/dot");

	@Parameters(name = "{0}")
	public static List<String[]> data() {
		final List<String[]> files = new ArrayList<String[]>();
		for (final File f : srcRootDir.listFiles())
			if (!f.isDirectory())
				files.add(new String[] { f.getPath(), new File(dotRootDir, f.getName()).getPath()});
		return files;
	}

	private ASTRoot root = null;
	private String dot = null;

	public TestPdg(final String src, final String dot) throws IOException {
		this.root = parse(load(src));
		this.dot = load(dot);
	}

	@Test
	public void testPdg() throws Exception {
		final Declaration d = root.getNamespacesList().get(0).getDeclarationsList().get(0);
		String s = "";
		for (final Method m : d.getMethodsList())
			s += pdgToDot(getpdg(m)) + "\n";
		assertEquals(dot, s);
	}

	protected String load(final String fileName) throws IOException {
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(fileName));
			final byte[] bytes = new byte[(int) new File(fileName).length()];
			in.read(bytes);
			return new String(bytes);
		} finally {
			if (in != null)
				in.close();
		}
	}
}
