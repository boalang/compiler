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
package boa.compiler.visitors;

import boa.compiler.ast.expressions.FunctionExpression;

/**
 * Finds if a tree has a function expression.
 * 
 * @author rdyer
 */
public class IsFunctionVisitor extends AbstractVisitorNoArg {
	private boolean isFunction;

	public boolean isFunction() {
		return isFunction;
	}

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		isFunction = false;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionExpression n) {
		isFunction = true;
	}
}
