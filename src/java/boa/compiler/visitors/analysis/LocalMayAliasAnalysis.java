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
import boa.compiler.ast.Operand;
import boa.compiler.ast.Pair;
import boa.compiler.ast.Selector;
import boa.compiler.ast.Term;
import boa.compiler.ast.UnaryFactor;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.statements.AssignmentStatement;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.types.*;
import boa.compiler.visitors.*;

/**
 * @author rramu
 */
public class LocalMayAliasAnalysis extends AbstractVisitorNoArg {
	HashSet<Identifier> aliastSet = new HashSet<Identifier>();
	Identifier lastDefVariable;
	boolean cloneFound = false;
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

	public final void dfs(final Node node, java.util.HashMap<Integer,String> nodeVisitStatus) {
		//if(node instanceof AssignmentStatement || node instanceof VarDeclStatement || node instanceof Call)
			node.accept(this);
		nodeVisitStatus.put(node.nodeId,"visited");
		for (Node succ : node.successors) {
		    if (nodeVisitStatus.get(succ.nodeId).equals("unvisited")) {
			dfs(succ, nodeVisitStatus);
		    }
		}
	}

	public HashSet<Identifier> start(CFGBuildingVisitor cfgBuilder, Identifier id) {
		aliastSet.add(id);
		java.util.HashMap<Integer,String> nodeVisitStatus = new java.util.HashMap<Integer,String>();
		for(Node subnode : cfgBuilder.order) {
			nodeVisitStatus.put(subnode.nodeId, "unvisited");
		}
		nodeVisitStatus.put(cfgBuilder.currentStartNodes.get(0).nodeId, "visited");
		dfs(cfgBuilder.currentStartNodes.get(0), nodeVisitStatus);
		//System.out.println("aliaset "+aliastSet);
		return aliastSet;
	}

	public void visit(final AssignmentStatement n) {
		Factor lhs = n.getLhs();
		lastDefVariable = (Identifier)n.getLhs().getOperand();
		if(n.getRhs().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand() instanceof Identifier && n.getRhs().getLhs().getLhs().getLhs().getLhs().getLhs().getOpsSize()==0) {
			boolean flag = false;
			for(Identifier alias : aliastSet) {
				if(((Identifier)n.getRhs().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand()).getToken().equals(alias.getToken())) {
					if(n.getRhs().getRhsSize()==0) {
						flag = true;
						break;
					}	
				}
			}
			if(flag) {
				aliastSet.add(lastDefVariable);
			}
		} else {
		n.getRhs().accept(this);
		}
	}

	public void visit(final Expression n) {
		n.getLhs().accept(this);
		for (final Conjunction c : n.getRhs())
			c.accept(this);
	}

	public void visit(final VarDeclStatement n) {
		lastDefVariable = n.getId();
		if(n.hasInitializer()) {
			if(n.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand() instanceof Identifier && n.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs().getOpsSize()==0) {
				boolean flag = false;
				for(Identifier alias : aliastSet) {
					if(((Identifier)n.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand()).getToken().equals(alias.getToken())) {
						if(n.getInitializer().getRhsSize()==0) {
							flag = true;
							break;
						}	
					}
				}
				if(flag) {
					aliastSet.add(lastDefVariable);
				}
			} else {
			n.getInitializer().accept(this);
			}
		}
	}

	public void visit(final Call n) {
		try {
			this.idFinder.start(n.env.getOperand());
			final String funcName = this.idFinder.getNames().toArray()[0].toString();
			final BoaFunction f = n.env.getFunction(funcName, check(n));
			if(funcName.equals("clone")) {
				cloneFound = true;
				visit(n.getArgs());
				cloneFound = false;
			}
		}
		catch(Exception e) {
		}
	}

	public void visit(final Identifier n) {
		try {
			if(cloneFound) {
				boolean flag = false;
				for(Identifier alias : aliastSet) {
					if(n.getToken().equals(alias.getToken())) {
						flag = true;
						break;
					}
				}
				if(flag) {
					aliastSet.add(lastDefVariable);
				}
			}
			cloneFound = false;
		}
		catch(Exception e) {
		}
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

	protected void visit(final List<? extends Node> nl) {
		for (final Node n : nl) {
			n.accept(this);
		}
	}
}
