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
package boa.compiler.visitors.analysis;

import java.util.*;

import boa.compiler.ast.Call;
import boa.compiler.ast.Comparison;
import boa.compiler.ast.Component;
import boa.compiler.ast.Composite;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Index;
import boa.compiler.ast.Node;
import boa.compiler.TypeCheckException;
import boa.compiler.ast.Operand;
import boa.compiler.ast.Pair;
import boa.compiler.ast.Selector;
import boa.compiler.ast.Term;
import boa.compiler.ast.UnaryFactor;
import boa.compiler.ast.expressions.Expression;
import boa.types.*;
import boa.compiler.visitors.*;

/**
 * @author rramu
 */
public class DataFlowSensitivityAnalysis extends AbstractVisitorNoArg {
	public boolean getValueFound = false;
	public HashSet<Identifier> getValueNodes = new HashSet<Identifier>();
	boolean flowSensitive = false;
	boolean argFlag = false;
	protected boolean abortGeneration = false;
	protected final IdentifierFindingVisitor idFinder = new IdentifierFindingVisitor();
	protected final CallFindingVisitor callFinder = new CallFindingVisitor();

	protected class CallFindingVisitor extends AbstractVisitorNoArg {
		protected boolean isCall;

		public boolean isCall() {
			return isCall;
		}

		/** {@inheritDoc} */
		@Override
		public void initialize() {
			super.initialize();
			isCall = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Factor n) {
			for (final Node node : n.getOps()) {
				isCall = false;
				node.accept(this);
			}
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Call n) {
			isCall = true;
		}
	}

	protected class IdentifierFindingVisitor extends AbstractVisitorNoArg {
		protected final Set<String> names = new HashSet<String>();

		public Set<String> getNames() {
			return names;
		}

		/** {@inheritDoc} */
		@Override
		protected void initialize() {
			names.clear();
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Identifier n) {
			names.add(n.getToken());
		}
	}

	public boolean isFlowSensitive() {
		return flowSensitive;
	}

	public final void dfs(final Node node, java.util.HashMap<Integer,String> nodeVisitStatus) {
		nodeVisitStatus.put(node.nodeId,"visited");
		node.accept(this);
		for (Node succ : node.successors) {
		    if (nodeVisitStatus.get(succ.nodeId).equals("unvisited")) {
			dfs(succ, nodeVisitStatus);
		    }
		}
	}

	public void start(CFGBuildingVisitor cfgBuilder, HashSet<Identifier> aliastSet) {
		java.util.HashMap<Integer,String> nodeVisitStatus = new java.util.HashMap<Integer,String>();
		for(Node subnode : cfgBuilder.order) {
			nodeVisitStatus.put(subnode.nodeId, "unvisited");
		}
		nodeVisitStatus.put(cfgBuilder.currentStartNodes.get(0).nodeId, "visited");
		dfs(cfgBuilder.currentStartNodes.get(0), nodeVisitStatus);

		for(Identifier getValueNode : getValueNodes) {
			for(Identifier alias : aliastSet) {
				if(!alias.getToken().equals(getValueNode.getToken())) {
					flowSensitive = true;
				}
			}
		}
	}

	public void visit(final Call n) {
		try {
		this.idFinder.start(n.env.getOperand());
		final String funcName = this.idFinder.getNames().toArray()[0].toString();
		final BoaFunction f = n.env.getFunction(funcName, check(n));
		if(funcName.equals("getvalue")) {
			if(n.getArgsSize()==1) {
				getValueFound = true;
				visit(n.getArgs());
				getValueFound = false;
			}
		}
		}
		catch(Exception e) {
		}
	}

	public void visit(final Identifier n) {
		if(getValueFound && argFlag) {
			getValueNodes.add((Identifier)n);
		}
		argFlag = false;
	}

	protected List<BoaType> check(final Call c) {
		if (c.getArgsSize() > 0)
			return this.check(c.getArgs());

		return new ArrayList<BoaType>();
	}

	protected List<BoaType> check(final List<Expression> el) {
		final List<BoaType> types = new ArrayList<BoaType>();

		for (final Expression e : el) {
			if (e.type instanceof BoaFunction) {
				callFinder.start(e);
				if (callFinder.isCall()) {
					types.add(((BoaFunction) e.type).getType());
					continue;
				}
			}

			types.add(e.type);
		}

		return types;
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
