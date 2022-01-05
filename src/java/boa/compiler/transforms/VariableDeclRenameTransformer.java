/*
 * Copyright 2018-2022, Robert Dyer, Che Shian Hung
 *                 Bowling Green State University
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

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import boa.compiler.ast.Call;
import boa.compiler.ast.Component;
import boa.compiler.ast.expressions.FunctionExpression;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.ast.Selector;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.types.FunctionType;
import boa.compiler.ast.types.OutputType;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;

import boa.types.BoaFunction;
import boa.types.BoaName;
import boa.types.BoaType;

/**
 * Finds and renames all variables based on the order of variable declaraction.
 * This allows the same variable names can be declared even in different scopes.
 *
 * @author rdyer
 * @author hungc
 */
public class VariableDeclRenameTransformer extends AbstractVisitorNoArgNoRet {
	int counter;
	Map<String, String> varHash = new HashMap<>();
	final Stack<Map<String, String>> hashStack = new Stack<>();

	/** {@inheritDoc} */
	@Override
	public void start(final Node n) {
		counter = 0;
		varHash.clear();
		super.start(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VarDeclStatement n) {
		final String oldId = n.getId().getToken();

		if (!(n.getType() instanceof OutputType)) {
			final String newId = oldId + "_" + counter++;
			n.getId().setToken(newId);
			varHash.put(oldId, newId);

			n.env.set(newId, n.env.get(oldId));
			n.env.removeLocal(oldId);
		}

		super.visit(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Selector n) {
		// do nothing, we dont want to rename the selector's identifier
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionExpression n) {
		hashStack.push(varHash);
		varHash = new HashMap<>(varHash);
		super.visit(n);
		varHash = hashStack.pop();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionType n) {
		super.visit(n);
		if (n.getArgsSize() > 0) {
			final BoaType[] params = new BoaType[n.getArgsSize()];
			int i = 0;
			for (final Component c : n.getArgs())
				params[i++] = new BoaName(c.getType().type, c.getIdentifier().getToken());
			((BoaFunction)n.type).setFormalParameters(params);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Identifier n) {
		final String oldId = n.getToken();
		if (!varHash.containsKey(oldId))
			return;

		final String newId = varHash.get(oldId);

		if (n.type != null || (n.env != null && !n.env.hasType(oldId) && !n.env.hasGlobal(oldId) && !n.env.hasGlobalFunction(oldId))) {
			n.setToken(newId);
			if (!n.env.hasLocal(newId))
				n.env.set(newId, n.env.get(oldId));
			if (n.env.hasLocal(oldId))
				n.env.removeLocal(oldId);
		}
	}
}
