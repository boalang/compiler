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
public class TestTypecheckBad extends BaseTest {
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

	@Test
	public void reDeclVar() throws IOException {
		typecheck(load(badDir + "re-decl-var.boa"), "variable 'f' already declared as 'function[]: any'");
	}

	@Test
	public void reDeclVar2() throws IOException {
		typecheck(load(badDir + "re-decl-var2.boa"), "variable 'i' already declared as 'int'");
	}

	@Test
	public void methodNoCall() throws IOException {
		typecheck(load(badDir + "method-no-call.boa"), "expected a call to function 'f'");
	}

	@Test
	public void methodCallWrongType() throws IOException {
		typecheck(load(badDir + "method-call-wrong-type.boa"), "no such function push([stack of int, stack of int])");
	}

	//@Test
	public void buildinMethodNoCall() throws IOException {
		typecheck(load(badDir + "builtin-method-no-call.boa"), "expected a call to function 'clear'");
	}

	//@Test
	public void quantMissingUse() throws IOException {
		typecheck(load(badDir + "quant-missing-use.boa"), "quantifier variable 'i' must be used in the foreach condition expression");
	}

	@Test
	public void currentBadType() throws IOException {
		typecheck(load(badDir + "current-badtype.boa"), "no such function current([int])");
	}

	@Test
	public void complexArray1() throws IOException {
		typecheck(load(badDir + "complex-arrays.boa"), "non-scalar/non-tuple type 'set of int' can not be used in arrays");
	}

	@Test
	public void complexArray2() throws IOException {
		typecheck(load(badDir + "complex-arrays2.boa"), "non-scalar/non-tuple type 'stack of int' can not be used in arrays");
	}

	@Test
	public void complexArray3() throws IOException {
		typecheck(load(badDir + "complex-arrays3.boa"), "non-scalar/non-tuple type 'map[string] of int' can not be used in arrays");
	}
}
