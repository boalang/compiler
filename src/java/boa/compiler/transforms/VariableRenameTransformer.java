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
package boa.compiler.transforms;

import java.util.Stack;

import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.ast.Selector;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * Finds and renames all variables in the tree, including their declarations
 * and uses.  Renames are done by adding a specified prefix to the front of
 * the names.
 * 
 * @author rdyer
 */
public class VariableRenameTransformer extends AbstractVisitorNoArg {
	protected String prefix = "_";
	protected final String visitArgName = "_n";

	/**
	 * Starts a variable renaming transformation with a given prefix.
	 * 
	 * @param n the node to start transform at
	 * @param prefix the prefix to add to the start of names
	 */
	public void start(Node n, String prefix) {
		this.prefix = prefix + "_";
		start(n);
	}

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
	public void visit(VisitStatement n) {
		// special case variable renaming for visit statements that name
		// their argument - this allows merging two visit statements from
		// different programs together, ensuring they use the same arg name
		if (n.hasComponent()) {
			oldVisitArgNames.push(oldVisitArgName);
			oldVisitArgName = n.getComponent().getIdentifier().getToken();
			n.getComponent().accept(this);
			n.getBody().accept(this);
			oldVisitArgName = oldVisitArgNames.pop();
			return;
		}
		super.visit(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(VarDeclStatement n) {
		final String oldId = n.getId().getToken();

		final String newId;
		if (n.getId().getToken().equals(oldVisitArgName))
			newId = visitArgName;
		else
			newId = prefix + oldId;
		n.env.set(newId, n.env.get(oldId));

		n.getId().accept(this);
		if (n.hasInitializer())
			n.getInitializer().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(Selector n) {
		// do nothing, we dont want to rename the selector's identifier
	}

	/** {@inheritDoc} */
	@Override
	public void visit(Identifier n) {
		final String oldId = n.getToken();

		if (n.env.hasType(oldId) || n.env.hasGlobal(oldId) || n.env.hasGlobalFunction(oldId))
			return;

		if (oldId.equals(oldVisitArgName))
			n.setToken(visitArgName);
		else
			n.setToken(prefix + oldId);
	}
}
