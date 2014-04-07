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
public class TestTypecheck extends BaseTest {
	final private static String rootDir = "test/typecheck/";
	final private static String badDir = rootDir + "errors/";


	@Test
	public void cout() throws IOException {
		typecheck(load(badDir + "cout.boa"), "type 'string' does not support the '<<' operator");
	}

	@Test
	public void assignTypeToVar() throws IOException {
		typecheck(load(badDir + "assign-type-to-var.boa"), "type 'Project' is not a value and can not be assigned");
	}

	@Test
	public void assignTypeToVar2() throws IOException {
		typecheck(load(badDir + "assign-type-to-var2.boa"), "type 'Project' is not a value and can not be assigned");
	}

	@Test
	public void varAsType() throws IOException {
		typecheck(load(badDir + "var-as-type.boa"), "type 'input' undefined");
	}
}
