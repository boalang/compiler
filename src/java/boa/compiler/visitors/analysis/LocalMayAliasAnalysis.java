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
 * See the License for the specif ic language governing permissions and
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
public class LocalMayAliasAnalysis extends AbstractVisitorNoArgNoRet {
	Set<Identifier> aliastSet = new LinkedHashSet<Identifier>();
	Identifier lastDefVariable;
	boolean cloneFound = false;
	protected final IdentifierFindingVisitor idFinder = new IdentifierFindingVisitor();
	protected final CallFindingVisitor callFinder = new CallFindingVisitor();

	public Set<Identifier> start(CFGBuildingVisitor cfgBuilder, Identifier id) {
		aliastSet.add(id);
		final Set<Integer> visitedNodes = new LinkedHashSet<Integer>();
		visitedNodes.add(cfgBuilder.currentStartNodes.get(0).nodeId);
		dfs(cfgBuilder.currentStartNodes.get(0), visitedNodes);
		return aliastSet;
	}

	public void visit(final AssignmentStatement n) {
		Factor lhs = n.getLhs();
		lastDefVariable = (Identifier)n.getLhs().getOperand();
		if (n.getRhs().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand() instanceof Identifier && n.getRhs().getLhs().getLhs().getLhs().getLhs().getLhs().getOpsSize() == 0) {
			boolean flag = false;
			for (final Identifier alias : aliastSet) {
				if (((Identifier)n.getRhs().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand()).getToken().equals(alias.getToken())) {
					if (n.getRhs().getRhsSize() == 0) {
						flag = true;
						break;
					}
				}
			}
			if (flag) {
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
		if (n.hasInitializer()) {
			if (n.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand() instanceof Identifier && n.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs().getOpsSize() == 0) {
				boolean flag = false;
				for (final Identifier alias : aliastSet) {
					if (((Identifier)n.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand()).getToken().equals(alias.getToken())) {
						if (n.getInitializer().getRhsSize() == 0) {
							flag = true;
							break;
						}
					}
				}
				if (flag) {
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
			if (funcName.equals("clone")) {
				cloneFound = true;
				visit(n.getArgs());
				cloneFound = false;
			}
		} catch (final Exception e) {
			// do nothing
		}
	}

	public void visit(final Identifier n) {
		try {
			if (cloneFound) {
				boolean flag = false;
				for (final Identifier alias : aliastSet) {
					if (n.getToken().equals(alias.getToken())) {
						flag = true;
						break;
					}
				}
				if (flag) {
					aliastSet.add(lastDefVariable);
				}
			}
			cloneFound = false;
		} catch (final Exception e) {
			// do nothing
		}
	}

	protected void visit(final List<? extends Node> nl) {
		for (final Node n : nl) {
			n.accept(this);
		}
	}
}
