/*
 * Copyright 2021, Robert Dyer,
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
package boa.compiler.visitors;

import boa.compiler.ast.expressions.FunctionExpression;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.ast.statements.AssignmentStatement;
import boa.compiler.ast.statements.VarDeclStatement;

/**
 * A visitor for the Boa abstract syntax tree.
 * This is the base of all visitors and provides useful helper functions.
 * 
 * @author rdyer
 */
public abstract class AbstractVisitorBase {
	public String getFunctionName(final FunctionExpression n) {
		final Node p = n.getParent().getParent().getParent().getParent().getParent().getParent().getParent();

		if (p instanceof AssignmentStatement)
			return ((Identifier)((AssignmentStatement)p).getLhs().getOperand()).getToken();

		if (p instanceof VarDeclStatement)
			return ((VarDeclStatement)p).getId().getToken();

		return null;
	}
}
