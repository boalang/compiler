/*
 * Copyright 2019, Hridesh Rajan, Robert Dyer, 
 *                 Iowa State University of Science and Technology
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
package boa.test.compiler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author rdyer
 */
@RunWith(Parameterized.class)
public class TestTypecheckBad extends BaseTest {
	final private static String rootDir = "test/typecheck/";
	final private static String badDir = rootDir + "errors/";

	@Parameters(name = "{index}][" + badDir + "{0}")
	public static Collection<Object[]> literals() {
		return Arrays.asList(new Object[][] {
			{ "cout.boa", "type 'string' does not support the '<<' operator" },
			{ "assign-type-to-var.boa", "type 'Project' is not a value and can not be assigned" },
			{ "assign-type-to-var2.boa", "type 'Project' is not a value and can not be assigned" },
			{ "var-as-type.boa", "type 'input' undefined" },
			{ "re-decl-var.boa", "variable 'f' already declared as 'function[]: any'" },
			{ "re-decl-var2.boa", "variable 'i' already declared as 'int'" },
			{ "method-no-call.boa", "expected a call to function 'f'" },
			{ "method-call-wrong-type.boa", "no such function push([stack of int, stack of int])" },
			//{ "builtin-method-no-call.boa", "expected a call to function 'clear'" },
			//{ "quant-missing-use.boa", "quantifier variable 'i' must be used in the foreach condition expression" },
			{ "current-badtype.boa", "no such function current([int])" },
			{ "after-return.boa", "return statement not allowed inside visitors" },
			{ "before-return.boa", "return statement not allowed inside visitors" },
			{ "nested-return.boa", "return statement not allowed inside visitors" },
			{ "assign-func-no-ret.boa", "functions without a return type can not be used as initializers" },
			{ "tuple-redecl.boa", "variable 'a' already declared as 'float'" },
			{ "bad-views1.boa", "subview 'bad' undefined" },
			{ "bad-views2.boa", "name conflict: identifier name 'subv' already exists" },
			{ "bad-views3.boa", "output variable 's' not found in subview 'sv'" },
			{ "bad-views5.boa", "invalid selector '_f'" },
			{ "bad-views6.boa", "invalid selector '_123f'" },
			{ "bad-views7.boa", "table column out of bounds" },
			{ "bad-views8.boa", "table column out of bounds" },
			{ "bad-views9.boa", "'table[count:string][year:string] of happy:int' has no member named 'bomb'" },
			{ "bad-views10.boa", "invalid index type 'int' for indexing into 'table[year:string] of happy:int' - expected 'year:string'" },
			{ "bad-views11.boa", "table column out of bounds" },
			{ "bad-views12.boa", "table column out of bounds" },
			{ "bad-views13.boa", "table column out of bounds" },
			{ "bad-views14.boa", "incompatible types for assignment: required 'table[year:int][projectID:string][fileName:string] of temp:int', found 'table[first:string][second:string] of third:int'" },
			{ "bad-views15.boa", "incompatible types for assignment: required 'tuple [int, string, string, int]', found 'tuple [string, string, int]'" },
			{ "bad-views16.boa", "incompatible types for assignment: required 'tuple [string, string, int]', found 'tuple [string, int]'" },
			{ "bad-views17.boa", "expect one operator and one term with operator '>>'" },
			{ "bad-views18.boa", "cannot assign row from table type 'table[year:int][projectID:string][fileName:string] of temp:int' to tuple type 'tuple [string, int]'" },
			{ "bad-identifier1.boa", "invalid identifier '_abc'" },
			{ "bad-identifier2.boa", "invalid identifier '_s'" },
			{ "bad-identifier3.boa", "invalid identifier '_f'" },
			{ "bad-identifier4.boa", "invalid identifier '_abc'" },
			{ "bad-identifier5.boa", "invalid identifier '_0'" },
			{ "bad-identifier6.boa", "invalid identifier '_0abc'" },
		});
	}

	private String file = null;
	private String err = null;

	public TestTypecheckBad(final String file, final String err) {
		this.file = file;
		this.err = err;
	}

	@Test
	public void testTypecheckBad() throws Exception {
		typecheck(load(badDir + file), err);
	}
}
