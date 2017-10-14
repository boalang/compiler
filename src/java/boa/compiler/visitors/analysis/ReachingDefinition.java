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
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.visitors.*;

/**
 * @author rramu
 */
public class ReachingDefinition extends AbstractVisitorNoArgNoRet {
	ArrayList<String> variablesMonitored = new ArrayList<String>();
	boolean inReturnStatement = false;

	HashMap<Integer,HashSet<Integer>> in = new HashMap<Integer,HashSet<Integer>>();
	HashMap<Integer,HashSet<Integer>> out = new HashMap<Integer,HashSet<Integer>>();
	ReachingDefinitionGen reachingDefinitionGen;
	ReachingDefinitionKill reachingDefinitionKill;
	int id = 0;

	public void cfgAnalysis(final Node node) {
		HashSet<Integer> current_in = new HashSet<Integer>();
		for(Node pred : node.predecessors) {
			if(out.containsKey(pred.nodeId)) {
				current_in.addAll(out.get(pred.nodeId));
			}
		}
		in.put(node.nodeId, (HashSet<Integer>)current_in.clone());
		if(reachingDefinitionKill.kill.containsKey(node.nodeId)) {
			HashSet<Integer> cur_kill = reachingDefinitionKill.kill.get(node.nodeId);
			current_in.removeAll(cur_kill);
		}
		if(reachingDefinitionGen.gen.containsKey(node.nodeId)) {
			Integer cur_gen = reachingDefinitionGen.gen.get(node.nodeId);
			current_in.add(cur_gen);
		}
		out.put(node.nodeId, (HashSet<Integer>)current_in.clone());
	}

	public final void createNodeIds(final Node node, java.util.HashMap<Node,String> nodeVisitStatus) {
		nodeVisitStatus.put(node,"visited");
		node.nodeId = ++id;
		for (Node succ : node.successors) {
		    if (nodeVisitStatus.get(succ).equals("unvisited")) {
			createNodeIds(succ, nodeVisitStatus);
		    }
		}
	}

	public final void display(final Node node, java.util.HashMap<Integer,String> nodeVisitStatus) {
		nodeVisitStatus.put(node.nodeId,"visited");
		for (Node succ : node.successors) {
		    if (nodeVisitStatus.get(succ.nodeId).equals("unvisited")) {
			display(succ, nodeVisitStatus);
		    }
		}
	}

	public void start(CFGBuildingVisitor cfgBuilder) {
		java.util.HashMap<Node,String> nodeVisitStatus1 = new java.util.HashMap<Node,String>();
		for(Node subnode : cfgBuilder.order) {
			nodeVisitStatus1.put(subnode, "unvisited");
		}
		nodeVisitStatus1.put(cfgBuilder.currentStartNodes.get(0), "visited");
		createNodeIds(cfgBuilder.currentStartNodes.get(0), nodeVisitStatus1);

		reachingDefinitionGen = new ReachingDefinitionGen();
		reachingDefinitionGen.start(cfgBuilder);

		reachingDefinitionKill = new ReachingDefinitionKill(reachingDefinitionGen.defs);
		reachingDefinitionKill.start(cfgBuilder);
		HashMap<Integer,HashSet<Integer>>  prev_out;
		HashMap<Integer,HashSet<Integer>>  cur_out;

		do {
			prev_out = (HashMap<Integer,HashSet<Integer>> )out.clone();
			java.util.HashMap<Integer,String> nodeVisitStatus = new java.util.HashMap<Integer,String>();
			for(Node subnode : cfgBuilder.order) {
				nodeVisitStatus.put(subnode.nodeId, "unvisited");
			}
			nodeVisitStatus.put(cfgBuilder.currentStartNodes.get(0).nodeId, "visited");
			dfs(cfgBuilder.currentStartNodes.get(0), nodeVisitStatus);
			cur_out = (HashMap<Integer,HashSet<Integer>> )out.clone();
		}while(!prev_out.equals(cur_out));
		if(cfgBuilder.getNodeById(id) instanceof boa.compiler.ast.statements.ReturnStatement) {
			String returnVariable = ((Identifier)((boa.compiler.ast.statements.ReturnStatement)cfgBuilder.getNodeById(id)).getExpr().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand()).getToken();
			HashSet<Integer> returnOut = out.get(id);
			for(int rout : returnOut) {
				if(cfgBuilder.getNodeById(rout) instanceof boa.compiler.ast.statements.AssignmentStatement) {
					if(((Identifier)((boa.compiler.ast.statements.AssignmentStatement)cfgBuilder.getNodeById(rout)).getLhs().getOperand()).getToken().equals(returnVariable)) {
						Expression rhs = ((boa.compiler.ast.statements.AssignmentStatement)cfgBuilder.getNodeById(rout)).getRhs();
						rhs.accept(this);	
					}
				}
				if(cfgBuilder.getNodeById(rout) instanceof boa.compiler.ast.statements.VarDeclStatement) {
					if(((boa.compiler.ast.statements.VarDeclStatement)cfgBuilder.getNodeById(rout)).getId().getToken().equals(returnVariable)) {
						if(((boa.compiler.ast.statements.VarDeclStatement)cfgBuilder.getNodeById(rout)).hasInitializer()) {
							((boa.compiler.ast.statements.VarDeclStatement)cfgBuilder.getNodeById(rout)).getInitializer().accept(this);
						}
					}
				}
			}
		}
	}

	public void visit(final Expression n) {
		n.getLhs().accept(this);
		for (final Conjunction c : n.getRhs())
			c.accept(this);
	}

	public void visit(final Comparison n) {
		n.getLhs().accept(this);
		if (n.hasRhs())
			n.getRhs().accept(this);
	}

	public void visit(final Component n) {
		if (n.hasIdentifier())
			n.getIdentifier().accept(this);
	}

	public void visit(final Composite n) {
	}

	public void visit(final Conjunction n) {
		n.getLhs().accept(this);
		for (final Comparison c : n.getRhs())
			c.accept(this);
	}

	public void visit(final Factor n) {
		n.getOperand().accept(this);
		for (final Node o : n.getOps())
			o.accept(this);
	}

	public void visit(final Identifier n) {
	}

	public void visit(final Term n) {
		n.getLhs().accept(this);
		for (final Factor f : n.getRhs())
			f.accept(this);
	}

	public void visit(final SimpleExpr n) {
		n.getLhs().accept(this);
		for (final Term t : n.getRhs())
			t.accept(this);
	}
}
