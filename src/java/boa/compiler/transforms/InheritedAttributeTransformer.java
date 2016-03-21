/*
 * Copyright 2016, Hridesh Rajan, Robert Dyer, Neha Bhide
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
package boa.compiler.transforms;

import boa.compiler.ast.Program;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * Converts use of current(Type) inherited attributes in visitors
 * into stack variables.
 * 
 * Logic
 * 
 * 1) Find if "current(T)" needs to be evaluated for any node. (Doubts : If Parser is not being modified do I check the program as plain text)
 * 2) If yes; Find how many times current is called and for what types.
 * 3) Create a stack of type T. (Doubt - Does Boa allow stacks of more than basic type ?)
 * 4) Modifying the Typechecked AST
 * 	* Every time encounter a node of type T during the visit of AST, add that node to stack.
 *      * Where-ever we encounter the function "current" for a type T, replace/add with node on the top of the stack<T>
 *      * Remember if there already is before/after code for the node(T) (then Push before the existing code and pop after the existing code)
 * 5) Confusion : GenerateCacheOutput and GenerateCacheVariable in LocalAggregationTransformer; their significance (How does it apply in this case)
 * 
 * @author rdyer
 * @author nbhide
 */
public class InheritedAttributeTransformer extends AbstractVisitorNoArg {
	/** {@inheritDoc} */
	@Override
	public void visit(final Program n) {
		// TODO
	}
}
