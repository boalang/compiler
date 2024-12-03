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
public class InformationAnalysis extends AbstractVisitorNoArgNoRet {
	public CFGBuildingVisitor cfgBuilder;
	public boolean unionFound = false;
	public boolean intersectionFound = false;
	public boolean addFound = false;
	public boolean removeFound = false;
	public Set<Identifier> getValueNodesAlias = new LinkedHashSet<Identifier>();
	public Set<Identifier> totalGetValueNodes = new LinkedHashSet<Identifier>();
	protected final IdentifierFindingVisitor idFinder = new IdentifierFindingVisitor();
	protected final CallFindingVisitor callFinder = new CallFindingVisitor();
	protected boolean abortGeneration = false;
	public boolean argFlag = false;
	public boolean genFlag = false;
	public boolean killFlag = false;
	Set<String> mergeOperation = new LinkedHashSet<String>(); 
	int satisfiedNodes = 0;

	public void start(final CFGBuildingVisitor cfgBuilder, final Set<Identifier> getValueNodesAlias, final Set<Identifier> totalGetValueNodes) {
		this.getValueNodesAlias = getValueNodesAlias;
		this.totalGetValueNodes = totalGetValueNodes;
		final Set<Integer> visitedNodes = new LinkedHashSet<Integer>();
		visitedNodes.add(cfgBuilder.currentStartNodes.get(0).nodeId);
		dfs(cfgBuilder.currentStartNodes.get(0), visitedNodes);
	}

	public void visit(final Call n) {
		try {
			this.idFinder.start(n.env.getOperand());
			final String funcName = this.idFinder.getNames().toArray()[0].toString();

			if (funcName.equals("union")) {
				unionFound = true;
				visit(n.getArgs());
				if (satisfiedNodes == 3) {
					mergeOperation.add("union");
				} else if(satisfiedNodes > 1 && satisfiedNodes < 3) {
					genFlag = true;
				}

				unionFound = false;
			}
			if (funcName.equals("intersect")) {
				intersectionFound = true;
				visit(n.getArgs());
					if (satisfiedNodes == 3) {
						mergeOperation.add("intersection");
					}
					else if (satisfiedNodes > 1 && satisfiedNodes < 3) {
						killFlag = true;
					}

				intersectionFound = false;
			}
			satisfiedNodes = 0;
			if (funcName.equals("add") || funcName.equals("union")) {
				addFound = true;
				visit(n.getArgs());
				addFound = false;
			} else if (funcName.equals("remove") || funcName.equals("difference")) {
				removeFound = true;
				visit(n.getArgs());
				removeFound = false;
			}
		} catch (final Exception e) {
			// do nothing
		}
	}

	public void visit(final Identifier n) {
		if (unionFound && argFlag) {	
			for (final Identifier getValueNodeAlias : getValueNodesAlias) {
				if (getValueNodeAlias.getToken().equals(n.getToken())) {
					satisfiedNodes++;
					break;
				}		
			}
			for (final Identifier getValueNodeAlias : totalGetValueNodes) {
				if (getValueNodeAlias.getToken().equals(n.getToken())) {
					satisfiedNodes++;
					break;
				}		
			}
			argFlag = false;
		}
		if (intersectionFound && argFlag) {
			for (final Identifier getValueNodeAlias : getValueNodesAlias) {
				if (getValueNodeAlias.getToken().equals(n.getToken())) {
					satisfiedNodes++;
					break;
				}		
			}
			for (final Identifier getValueNodeAlias : totalGetValueNodes) {
				if (getValueNodeAlias.getToken().equals(n.getToken())) {
					satisfiedNodes++;
					break;
				}		
			}
			argFlag = false;
		}

		if (addFound && argFlag) {
			for (final Identifier getValueNodeAlias : getValueNodesAlias) {
				if (getValueNodeAlias.getToken().equals(n.getToken())) {
					genFlag = true;
					break;
				}		
			}
			argFlag = false;
		}
		if (removeFound && argFlag) {
			for (final Identifier getValueNodeAlias : getValueNodesAlias) {
				if (getValueNodeAlias.getToken().equals(n.getToken())) {
					killFlag = true;
					break;
				}		
			}
			argFlag = false;
		}
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
