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
package boa.test.datagen;

import java.util.*;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;

import boa.datagen.util.Java8Visitor;

/*
 * @author rdyer
 */
public class Java8BaseTest extends Java7BaseTest {
	private static void setJava8() {
		astLevel = AST.JLS8;
		javaVersion = JavaCore.VERSION_1_8;
		visitor = new Java8Visitor("", new HashMap<String, Integer>());
	}

	public static String parseJava(final String content) {
		setJava8();
		return Java7BaseTest.parseJava(content);
	}

	public static void testWrapped(final String java, final String expected) {
		setJava8();
		Java7BaseTest.testWrapped(java, expected);
	}

	protected static String parseWrapped(final String content) {
		setJava8();
		return Java7BaseTest.parseWrapped(content);
	}
}
