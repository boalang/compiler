/*
 * Copyright 2017, Hridesh Rajan, Robert Dyer, Ramanathan Ramu
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
package boa.compiler.visitors.analysis;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import boa.compiler.ast.Call;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;
import boa.compiler.visitors.CFGBuildingVisitor;
import boa.compiler.visitors.CallFindingVisitor;
import boa.compiler.visitors.IdentifierFindingVisitor;

/**
 * @author rramu
 */
public class DataFlowSensitivityAnalysis extends AbstractVisitorNoArgNoRet {
	public boolean getValueFound = false;
	public Set<Identifier> getValueNodes = new LinkedHashSet<Identifier>();
	boolean flowSensitive = false;
	boolean argFlag = false;
	protected boolean abortGeneration = false;
	protected final IdentifierFindingVisitor idFinder = new IdentifierFindingVisitor();
	protected final CallFindingVisitor callFinder = new CallFindingVisitor();

	public boolean isFlowSensitive() {
		return flowSensitive;
	}

	public void start(final CFGBuildingVisitor cfgBuilder, final Set<Identifier> aliastSet) {
		final Set<Integer> visitedNodes = new LinkedHashSet<Integer>();
		visitedNodes.add(cfgBuilder.currentStartNodes.get(0).nodeId);
		dfs(cfgBuilder.currentStartNodes.get(0), visitedNodes);

		for (final Identifier getValueNode : getValueNodes) {
			for (final Identifier alias : aliastSet) {
				if (!alias.getToken().equals(getValueNode.getToken())) {
					flowSensitive = true;
				}
			}
		}
	}

	public void visit(final Call n) {
		try {
			this.idFinder.start(n.env.getOperand());
			final String funcName = this.idFinder.getNames().toArray()[0].toString();
			if (funcName.equals("getvalue")) {
				if (n.getArgsSize() == 1) {
					getValueFound = true;
					visit(n.getArgs());
					getValueFound = false;
				}
			}
		} catch (final Exception e) {
			// do nothing
		}
	}

	public void visit(final Identifier n) {
		if (getValueFound && argFlag) {
			getValueNodes.add((Identifier)n);
		}
		argFlag = false;
	}

	public void visit(final Factor n) {
		if (n.getOpsSize() > 0) {
			n.env.setOperand(n.getOperand());
			abortGeneration = false;

			if (!(n.getOp(0) instanceof Call)) {
				n.getOperand().accept(this);
				n.env.setOperandType(n.getOperand().type);
			}

			for (int i = 0; !abortGeneration && i < n.getOpsSize(); i++) {
				final Node o = n.getOp(i);
				o.accept(this);
			}
		} else {
			n.getOperand().accept(this);
		}
	}

	protected void visit(final List<? extends Node> nl) {
		for (final Node n : nl) {
			argFlag = true;
			n.accept(this);
		}
	}
}
