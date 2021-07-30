/*
 * Copyright 2016, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology,
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
package boa.test.datagen.java;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;

import boa.datagen.DefaultProperties;
import boa.datagen.util.JavaVisitor;

/*
 * @author rdyer
 */
public class Java15BaseTest extends Java7BaseTest {
	private static void setJava15() {
		astLevel = DefaultProperties.DEFAULT_JAVA_ASTLEVEL;
		javaVersion = JavaCore.VERSION_15;
		visitor = new JavaVisitor("");
	}

	public static void dumpJavaWrapped(final String content) {
		setJava15();
		Java7BaseTest.dumpJavaWrapped(content);
    }

	public static void dumpJava(final String content) {
		setJava15();
		Java7BaseTest.dumpJava(content);
	}

	public static String parseJava(final String content) {
		setJava15();
		return Java7BaseTest.parseJava(content);
	}

	public static void testWrapped(final String java, final String expected) {
		setJava15();
		Java7BaseTest.testWrapped(java, expected);
	}

	protected static String parseWrapped(final String content) {
		setJava15();
		return Java7BaseTest.parseWrapped(content);
	}
}
