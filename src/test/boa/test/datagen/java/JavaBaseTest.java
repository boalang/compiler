/*
 * Copyright 2016-2022, Hridesh Rajan, Robert Dyer, Huaiyao Ma,
 *                 Iowa State University of Science and Technology,
 *                 Bowling Green State University,
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
import java.util.UUID;

import boa.datagen.util.FileIO;
import boa.functions.langmode.JavaLangMode;
import boa.test.compiler.BaseTest;


/*
 * @author rdyer
 * @author huaiyao
 */
public class JavaBaseTest extends BaseTest {
	protected static void setJavaVersion() {
		JavaLangMode.astLevel = JavaLangMode.DEFAULT_JAVA_ASTLEVEL;
		JavaLangMode.javaVersion = JavaLangMode.DEFAULT_JAVA_CORE;
	}

	protected static void dumpJava(final String content) {
		setJavaVersion();
		JavaLangMode.dumpJava(content);
	}

	public static String parseJava(final String content) {
		final File f = new File(new File(System.getProperty("java.io.tmpdir")), UUID.randomUUID().toString());
		try {
			FileIO.writeFileContents(f, content);
			return parseJavaFile(f.getPath());
		} finally {
			try {
				FileIO.delete(f);
			} catch (final Exception e) {}
		}
	}

	public static String parseJavaFile(final String path) {
		setJavaVersion();
		return FileIO.normalizeEOL(JavaLangMode.parseJavaFile(path));
	}

	protected static void dumpJavaWrapped(final String content) {
		dumpJava(getWrapped(content));
	}

	protected static String parseWrapped(final String content) {
		return parseJava(getWrapped(content));
	}

	protected static String getWrapped(final String content) {
		String s = "class t {\n   void m() {\n      " + content.replaceAll("\n", "\n      ").trim();
		if (content.indexOf(";") == -1 && !s.endsWith("}"))
			s += ";";
		if (!s.endsWith("\n"))
			s += "\n";
		s += "   }\n}";
		return s;
	}

	protected static String getWrappedResult(final String expected) {
		return "{\n"
				+ "   \"namespaces\": [\n"
				+ "      {\n"
				+ "         \"name\": \"\",\n"
				+ "         \"declarations\": [\n"
				+ "            {\n"
				+ "               \"name\": \"t\",\n"
				+ "               \"kind\": \"CLASS\",\n"
				+ "               \"methods\": [\n"
				+ "                  {\n"
				+ "                     \"name\": \"m\",\n"
				+ "                     \"return_type\": {\n"
				+ "                        \"name\": \"void\",\n"
				+ "                        \"kind\": \"PRIMITIVE\"\n"
				+ "                     },\n"
				+ "                     \"statements\": [\n"
				+ "                        {\n"
				+ "                           \"kind\": \"BLOCK\",\n"
				+ "                           \"statements\": [\n"
				+ "                              " + expected.replaceAll("\n", "\n                              ") + "\n"
				+ "                           ]\n"
				+ "                        }\n"
				+ "                     ]\n"
				+ "                  }\n"
				+ "               ],\n"
				+ "               \"fully_qualified_name\": \"t\"\n"
				+ "            }\n"
				+ "         ]\n"
				+ "      }\n"
				+ "   ]\n"
				+ "}";
	}
}
