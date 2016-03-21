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
 * Converts use of current(T) inherited attributes in visitors into stack variables.
 * 
 * General algorithm:
 * 
 * 1) Find all instances of "current(T)" in the AST
 * 2) Collect set of all unique types T found in #1
 * 3) For each type T in the set from #2:
 *    a) Add a variable 's_T' of type 'stack of T' at the top-most scope of the AST
 *    b) Where-ever we encounter 'current(T)', replace with code for 's_T.peek()'
 *    c) Add/Update the before clause for T
 *       i)  If the visitor has a 'before T' clause, add 's_t.push(node)' as the first statement
 *       ii) Otherwise, add a 'before T' clause with a 's_t.push(node)'
 *    d) Add/Update the after clause for T
 *       i)  If the visitor has a 'after T' clause, add 's_t.pop()' as the first statement
 *       ii) Otherwise, add a 'after T' clause with a 's_t.pop()'
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
