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

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author rdyer
 */
@RunWith(JUnit4.class)
public class TestTraversalBad extends BaseTest {
	final private static String rootDir = "test/traversal/";
	final private static String badDir = rootDir + "errors/";

	@Test
	public void traversalWithStop() throws IOException {
		typecheck(load(badDir + "traversal-with-stop.boa"), "Stop statement only allowed inside 'before' visits");
	}

	@Test
	public void traversalWithNoReturn() throws IOException {
		codegen(load(badDir + "traverse-with-no-return-statement.boa"), "Error on line 134: missing return statement");
	}
}
