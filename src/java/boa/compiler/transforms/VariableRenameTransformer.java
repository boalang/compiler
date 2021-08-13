/*
 * Copyright 2014-2021, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology
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
package boa.compiler.transforms;

import java.util.Stack;

import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.ast.Selector;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * Finds and renames visitor statements that name their argument.
 * This allows merging two visit statements from different programs together,
 * ensuring they use the same arg name.
 *
 * @author rdyer
 */
public class VariableRenameTransformer extends AbstractVisitorNoArg {
	protected final String visitArgName = "_n";

	protected String oldVisitArgName;
	protected final Stack<String> oldVisitArgNames = new Stack<String>();

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		oldVisitArgName = null;
		oldVisitArgNames.clear();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n) {
		if (n.hasComponent()) {
			oldVisitArgNames.push(oldVisitArgName);
			oldVisitArgName = n.getComponent().getIdentifier().getToken();
			n.getComponent().accept(this);
			n.getBody().accept(this);
			n.getBody().env.set(visitArgName, n.getBody().env.get(oldVisitArgName));
			n.getBody().env.removeLocal(oldVisitArgName);
			oldVisitArgName = oldVisitArgNames.pop();
			return;
		}
		super.visit(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VarDeclStatement n) {
		final String oldId = n.getId().getToken();

		if (n.getId().getToken().equals(oldVisitArgName)) {
			n.env.set(visitArgName, n.env.get(oldId));
			n.env.removeLocal(oldId);

			n.getId().accept(this);
			if (n.hasInitializer())
				n.getInitializer().accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Selector n) {
		// do nothing, we dont want to rename the selector's identifier
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Identifier n) {
		final String oldId = n.getToken();

		if (n.env.hasType(oldId) || n.env.hasGlobal(oldId) || n.env.hasGlobalFunction(oldId))
			return;

		if (oldId.equals(oldVisitArgName))
			n.setToken(visitArgName);
	}
}
