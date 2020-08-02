/*
 * Copyright 2018, Hridesh Rajan, Ganesha Upadhyaya, Robert Dyer, Mohd Arafat,
 *                 Bowling Green State University
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
package boa.graphs.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import boa.functions.BoaAstIntrinsics;
import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Method;
import boa.types.Ast.Statement;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Variable;
import boa.types.Control.Edge.EdgeLabel;
import boa.types.Control.Graph.Builder;
import boa.types.Control.Node.NodeType;

/**
 * Control flow graph builder.
 *
 * @author ganeshau
 * @author rdyer
 * @author marafat
 */
public class CFG {
	protected Method md;
	protected String class_name;

	protected final HashSet<CFGNode> nodes = new HashSet<CFGNode>();
	protected CFGNode entryNode;
	protected CFGNode exitNode;

	protected final HashSet<CFGNode> outs = new HashSet<CFGNode>();
	protected final HashSet<CFGNode> ins = new HashSet<CFGNode>();
	protected final HashSet<CFGNode> breaks = new HashSet<CFGNode>();
	protected final HashSet<CFGNode> returns = new HashSet<CFGNode>();

	protected boolean isLoopPresent = false;
	protected boolean isBranchPresent = false;
	protected boolean paramAsStatement = false;

	public CFG(final Method method) {
		this(method, "this");
	}

	public CFG(final Method method, final String cls_name) {
		this.md = method;
		this.class_name = cls_name;
	}

	public CFG(final Method method, boolean paramAsStatement) {
		this.md = method;
		this.class_name = "this";
		this.paramAsStatement = paramAsStatement;
	}

	public CFG() {
	}

	public Method getMd() {
		return md;
	}

	public boolean getIsLoopPresent() {
		return isLoopPresent;
	}

	public boolean getIsBranchPresent() {
		return isBranchPresent;
	}

	public String getClass_name() {
		return class_name;
	}

	public HashSet<CFGNode> getNodes() {
		return nodes;
	}

	public HashSet<CFGNode> getOuts() {
		return outs;
	}

	public HashSet<CFGNode> getIns() {
		return ins;
	}

	public CFGNode getEntryNode() {
		return entryNode;
	}

	public CFGNode getExitNode() {
		return exitNode;
	}

	/**
	 * Returns the CFG node if id exists, null otherwise
	 *
	 * @param id
	 * @return CFG node
	 */
	public CFGNode getNode(int id) {
		for (final CFGNode node : nodes) {
			if (node.getNodeId() == id)
				return node;
		}
		return null;
	}

	protected void addNode(final CFGNode node) {
		if (nodes.contains(node))
			return;
		outs.add(node);
		nodes.add(node);
		ins.add(node);
	}

	protected void removeNode(final CFGNode node) {
		if (!nodes.contains(node))
			return;
		nodes.remove(node);
		ins.remove(node);
		outs.remove(node);
	}

	/*
	 * merge two graph together merge outs with target's ins update outs and ins
	 */
	protected void mergeSeq(final CFG target) {
		// ignore empty graph
		if (target.getNodes().size() == 0)
			return;

		if (nodes.size() == 0) {
			nodes.addAll(target.nodes);
			ins.addAll(target.ins);
			outs.addAll(target.outs);
			breaks.addAll(target.breaks);
			returns.addAll(target.returns);
			return;
		}

		// add Nodes
		nodes.addAll(target.getNodes());

		// merge Edges
		for (final CFGNode aNode : outs) {
			for (final CFGNode anoNode : target.ins) {
				createNewEdge(aNode, anoNode);
			}
		}

		// keep only outs of right most child iff the child has outs
		outs.clear();
		outs.addAll(target.outs);

		breaks.addAll(target.breaks);
		returns.addAll(target.returns);
	}

	protected void createNewEdge(final CFGNode src, final CFGNode dest) {
		createNewEdge(src, dest, null);
	}

	protected void createNewEdge(final CFGNode src, final CFGNode dest, final String label) {
		if (src.getSuccessors().contains(dest))
			return;

		if (label == null)
			new CFGEdge(src, dest);
		else
			new CFGEdge(src, dest, label);
	}

	protected void mergeSeq(final CFGNode branch) {
		this.addNode(branch);
		/*
		 * branch will not be considered ins node except the input graph is size 0
		 */
		ins.remove(branch);
		for (final CFGNode aNode : outs) {
			if (!aNode.equals(branch)) {
				createNewEdge(aNode, branch);
			}
		}

		if (ins.size() == 0)
			ins.add(branch);
		outs.clear();
		outs.add(branch);
	}

	protected void mergeBranches(final CFG target, final HashSet<CFGNode> saveOuts) {
		if (target.getNodes().size() == 0)
			return;

		if (saveOuts.size() == 0) {
			// add Nodes
			nodes.addAll(target.nodes);
			// merge Edges
			ins.addAll(target.ins);
			outs.addAll(target.outs);
			return;
		}

		// add Nodes
		nodes.addAll(target.nodes);
		// merge Edges
		for (final CFGNode aNode : saveOuts) {
			for (final CFGNode anoNode : target.ins) {
				createNewEdge(aNode, anoNode);
			}
		}
		outs.addAll(target.outs);
	}

	protected void mergeABranch(final CFG target, final CFGNode branch) {
		mergeABranch(target, branch, null);
	}

	protected void mergeABranch(final CFG target, final CFGNode branch, final String label) {
		// merge Node
		if (target.getNodes().size() == 0)
			return;

		// add Nodes
		nodes.addAll(target.nodes);
		// merge Edges
		for (final CFGNode aNode : target.ins) {
			createNewEdge(branch, aNode, label);
		}
		// keep all outs node of children
		outs.addAll(target.outs);
		breaks.addAll(target.breaks);
		returns.addAll(target.returns);
	}

	protected void addBackEdges(final CFG target, final CFGNode branch, final String label) {
		for (final CFGNode aNode : target.outs) {
			createNewEdge(aNode, branch, label);
		}
	}

	protected void addBreakNode(final String identifier) {
		addBreakNode(new CFGNode(identifier, NodeType.OTHER, "<GOTO>", identifier));
	}

	protected void addBreakNode(final CFGNode node) {
		this.mergeSeq(node);
		this.outs.remove(node);
		this.breaks.add(node);
	}

	protected void addReturnNode() {
		addReturnNode(new CFGNode("END[return]", NodeType.OTHER, "END[return]", "END[return]"));
	}

	protected void addReturnNode(final CFGNode node) {
		this.mergeSeq(node);
		this.outs.remove(node);
		this.returns.add(node);
	}

	protected void adjustBreakNodes(final String id) {
		for (final CFGNode node : new ArrayList<CFGNode>(this.breaks)) {
			if (node.getObjectName().equals(id)) {
				this.outs.add(node);
				this.breaks.remove(node);
			}
		}
	}

	protected void adjustReturnNodes() {
		this.outs.addAll(this.returns);
		this.returns.clear();
	}

	protected Statement getStatement() {
		final Statement.Builder stm = Statement.newBuilder();
		stm.setKind(StatementKind.BLOCK);

		for (final Variable v : md.getArgumentsList()) { //FIXME assign proper type values
			final Expression exp = BoaAstIntrinsics.parseexpression(v.getName() + "= 1");
			final Statement.Builder stm1 = Statement.newBuilder();
			stm1.setKind(StatementKind.EXPRESSION);
			stm1.addExpressions(exp);
			stm.addStatements(stm1.build());
		}
		stm.addAllStatements(md.getStatementsList());

		return stm.build();
	}

	public boolean isEmpty() {
		return this.nodes.isEmpty();
	}

	public CFG get() {
		if (md.getStatementsCount() > 0) {
			CFGNode.numOfNodes = -1;
			final CFGNode startNode = new CFGNode("ENTRY", NodeType.ENTRY, "ENTRY", "ENTRY");
			mergeSeq(startNode);
			if (paramAsStatement)
				mergeSeq(traverse(startNode, getStatement()));
			else
				mergeSeq(traverse(startNode, md.getStatements(0)));

			adjustReturnNodes();
			final CFGNode endNode = new CFGNode("EXIT", NodeType.ENTRY, "EXIT", "EXIT");
			mergeSeq(endNode);
			this.exitNode = endNode;

			this.entryNode = startNode;

			// if this happens, the original AST was invalid
			if (returns.size() > 0 || breaks.size() > 0) {
				return null;
			}
		}
		return this;
	}

	private CFG traverse(final CFGNode cfgNode, final Expression root) {
		final CFG graph = new CFG();
		switch (root.getKind()) {
		case CONDITIONAL:
			return traverse_conditional(cfgNode, root);
		case METHODCALL:
			if (root.getMethod().equals("<init>"))
				return traverse_init(cfgNode, root);
			if (root.getMethod().equals("super"))
				return traverse_clinit(cfgNode, root);
			if (root.getMethod().contains("super."))
				return traverse_super(cfgNode, root);
			return traverse_call(cfgNode, root);
		case NEW:
			return traverse_instance(cfgNode, root);
		case YIELD:
			return traverse_yield(cfgNode, root);
		default:
			/*
			 * final List<Expression> expressionsList =
			 * root.getExpressionsList(); final int expressionsSize =
			 * expressionsList.size(); for (int i = 0; i < expressionsSize; i++)
			 * { graph.mergeSeq(traverse(cfgNode, expressionsList.get(i))); }
			 */
			final CFGNode bNode = new CFGNode(root.getKind().name(), NodeType.OTHER, "", root.getKind().name());
			bNode.setAstNode(root);
			graph.mergeSeq(bNode);
			return graph;
		}
	}

	private CFG traverse(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		switch (root.getKind()) {
		case BLOCK:
			for (final Statement stmt : root.getStatementsList())
				graph.mergeSeq(traverse(cfgNode, stmt));
			return graph;
		case EXPRESSION:
			return traverse(cfgNode, root.getExpressions(0));
		case SYNCHRONIZED:
			return traverse_sync(cfgNode, root);
		case RETURN:
			return traverse_return(cfgNode, root);
		case FOR:
		case FOREACH:
			return traverse_for(cfgNode, root);
		case DO:
			return traverse_do(cfgNode, root);
		case WHILE:
			return traverse_while(cfgNode, root);
		case IF:
			return traverse_if(cfgNode, root);
		case BREAK:
			return traverse_break(cfgNode, root);
		case WITH:
			return traverse_with(cfgNode, root);
		case CASE:
			return traverse_case(cfgNode, root);
		case DEFAULT:
			return traverse_case(cfgNode, root);
		case CONTINUE:
			return traverse_continue(cfgNode, root);
		case LABEL:
			return traverse_labeled(cfgNode, root);
		case SWITCH:
			return traverse_switch(cfgNode, root);
		case TRY:
			return traverse_try(cfgNode, root);
		case THROW:
		case RAISE:
			return traverse_throw(cfgNode, root);
		case CATCH:
			return traverse_catch(cfgNode, root);
		case FINALLY:
			return traverse_finally(cfgNode, root);
		case ASSERT:
		case TYPEDECL:
		case EMPTY:
		case DEL:
		case GLOBAL:
		case OTHER:
			final CFGNode aNode = new CFGNode(root.getKind().name(), NodeType.OTHER, "", root.getKind().name());
			aNode.setAstNode(root);
			graph.mergeSeq(aNode);
			return graph;
		default:
			return graph;
		}
	}

	private CFG traverse_instance(final CFGNode cfgNode, final Expression root) {
		final CFG graph = new CFG();
		String nObjectname;
		if (root.hasVariable() && !root.getVariable().isEmpty()) {
			final Variable pNode = root.getVariableDecls(0); // TODO check for multiple var_decls
			nObjectname = pNode.getName();
		} else if ((root.getExpressionsCount() > 0) && root.getExpressions(0).getKind() == ExpressionKind.ASSIGN) {
			final Expression ex = root.getExpressions(0);
			if (ex.getKind() == ExpressionKind.VARACCESS) {
				nObjectname = ex.getVariable();
			} else {
				nObjectname = ex.toString();
			}
		} else {
			nObjectname = "anonymous";
		}

		final String type_str;
		if (root.getVariableDeclsCount() > 0) {
			type_str = String.valueOf(root.getVariableDecls(0).getVariableType().getName());
		} else if (root.hasNewType()) {
			type_str = String.valueOf(root.getNewType().getName());
		} else {
			type_str = "";
		}

		final CFGNode aNode = new CFGNode("$new$", NodeType.METHOD, type_str, nObjectname, root.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getNodeId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_clinit(final CFGNode cfgNode, final Expression root) {
		final CFG graph = new CFG();
		final CFGNode aNode = new CFGNode("<init>", NodeType.METHOD, class_name, "super", root.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getNodeId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_init(final CFGNode cfgNode, final Expression root) {
		final CFG graph = new CFG();
		final CFGNode aNode = new CFGNode("<init>", NodeType.METHOD, class_name, "this", root.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getNodeId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_call(final CFGNode cfgNode, final Expression root) {
		final CFG graph = new CFG();
		final CFGNode aNode = new CFGNode(root.getMethod(), NodeType.METHOD, class_name, "this", root.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getNodeId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_super(final CFGNode cfgNode, final Expression root) {
		final CFG graph = new CFG();
		final CFGNode aNode = new CFGNode(root.getMethod(), NodeType.METHOD, class_name, "super", root.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getNodeId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_if (final CFGNode cfgNode, final Statement root) {
		this.isBranchPresent = true;
		final CFG graph = new CFG();

		final CFGNode branch = new CFGNode("IF", NodeType.CONTROL, "IF", "IF");
		branch.setAstNode(root);
		branch.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getNodeId() + ".");
		graph.mergeSeq(branch);

		boolean trueNotEmpty = false, falseNotEmpty = false;
		if (root.getStatementsCount() > 0) { // Then
			final CFG trueBranch = traverse(branch, root.getStatements(0));
			if (trueBranch.getNodes().size() > 0) {
				graph.mergeABranch(trueBranch, branch, "T");
				trueNotEmpty = true;
			}
		}
		if (root.getStatementsCount() > 1) { // Else
			final CFG falseBranch = traverse(branch, root.getStatements(1));
			if (falseBranch.getNodes().size() > 0) {
				graph.mergeABranch(falseBranch, branch, "F");
				falseNotEmpty = true;
			}
		}
		if (trueNotEmpty && falseNotEmpty)
			graph.getOuts().remove(branch);
		return graph;
	}

	private CFG traverse_conditional(final CFGNode cfgNode, final Expression root) {
		this.isBranchPresent = true;
		final CFG graph = new CFG();

		final CFGNode branch = new CFGNode("IF", NodeType.CONTROL, "IF", "IF");
		branch.setAstNode(root);
		branch.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getNodeId() + ".");
		graph.mergeSeq(branch);

		boolean trueNotEmpty = false, falseNotEmpty = false;
		if (root.getExpressionsCount() > 0) { // Then
			final CFG trueBranch = traverse(branch, root.getExpressions(0));
			if (trueBranch.getNodes().size() > 0) {
				graph.mergeABranch(trueBranch, branch, "T");
				trueNotEmpty = true;
			}
		}
		if (root.getExpressionsCount()==3) { // Else
			final CFG falseBranch = traverse(branch, root.getExpressions(2));
			if (falseBranch.getNodes().size() > 0) {
				graph.mergeABranch(falseBranch, branch, "F");
				falseNotEmpty = true;
			}
		}
		else if (root.getExpressionsCount() > 1) { // Else
			final CFG falseBranch = traverse(branch, root.getExpressions(1));
			if (falseBranch.getNodes().size() > 0) {
				graph.mergeABranch(falseBranch, branch, "F");
				falseNotEmpty = true;
			}
		}
		if (trueNotEmpty && falseNotEmpty)
			graph.getOuts().remove(branch);
		return graph;
	}

	private CFG traverse_switch(final CFGNode cfgNode, final Statement root) {
		this.isBranchPresent = true;
		final CFG graph = new CFG();
		final CFGNode node = new CFGNode(root.getExpressions(0).toString(), NodeType.CONTROL, "SWITCH", "SWITCH");
		node.setAstNode(root.getExpressions(0));
		node.setPid((cfgNode == null) ? "." : cfgNode.getPid()+ cfgNode.getNodeId() + ".");
		graph.mergeSeq(node);
		CFG subgraph = null;
		Statement sc = null;
		boolean hasdefault = false;
		for (final Statement s : root.getStatementsList()) {
			if (s.getKind() == StatementKind.CASE || s.getKind() == StatementKind.DEFAULT) {
				final CFG lastsub = subgraph;
				subgraph = new CFG();
				subgraph.mergeSeq(traverse(node, s));
				if (lastsub != null) {
					for (final CFGNode out : lastsub.outs) {
						for (final CFGNode in : subgraph.ins) {
							createNewEdge(out, in);
						}
					}
					lastsub.outs.clear();
					if (sc.getExpressionsCount() > 0)
						graph.mergeABranch(lastsub, node, sc.getExpressions(0).toString());
					else
						graph.mergeABranch(lastsub, node);
				}
				sc = s;
				if (s.getKind() == StatementKind.DEFAULT)
					hasdefault = true;
			} else {
				subgraph.mergeSeq(traverse(node, s));
			}
		}
		if (subgraph != null) {
			if (sc.getExpressionsCount() > 0)
				graph.mergeABranch(subgraph, node, sc.getExpressions(0).toString());
			else
				graph.mergeABranch(subgraph, node);
		}
		if (hasdefault)
			graph.getOuts().remove(node);
		graph.adjustBreakNodes("[BREAK]");
		return graph;
	}

	private CFG traverse_for(final CFGNode cfgNode, final Statement root) {
		this.isLoopPresent = true;
		final CFG graph = new CFG();

		// initializations
		if (root.hasVariableDeclaration()) {
			// enhanced for
			final CFGNode bNode = new CFGNode("VARDECL", NodeType.OTHER, "", "VARDECL");
			final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
			eb.setKind(boa.types.Ast.Expression.ExpressionKind.VARDECL);
			eb.addVariableDecls(root.getVariableDeclaration());
			bNode.setAstNode(eb.build());
			graph.mergeSeq(bNode);
		} else {
			// normal for
			for (final Expression e : root.getInitializationsList()) {
				graph.mergeSeq(traverse(cfgNode, e));
			}
		}

		// condition
		final CFGNode control = new CFGNode("FOR", NodeType.CONTROL, "FOR", "FOR");
		control.setAstNode(root);
		control.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getNodeId() + ".");
		graph.mergeSeq(control);

		// body
		final CFG body = traverse(control, root.getStatements(0));

		body.adjustBreakNodes("[CONTINUE]");

		// updates
		for (final Expression e : root.getUpdatesList()) {
			final CFG update = traverse(cfgNode, e);
			body.mergeSeq(update);
		}

		graph.mergeABranch(body, control, "T");
//		graph.addBackEdges(body, control, "B");

		boolean trueNotEmpty = false, falseNotEmpty = false;

		if (root.getStatementsCount() > 1) { // Else
			this.isBranchPresent = true;
			final CFG falseBranch = traverse(control, root.getStatements(1));
			if (falseBranch.getNodes().size() > 0) {
				falseNotEmpty = true;
				graph.mergeABranch(falseBranch, control, "F");
			}
		}
		
		//graph.getOuts().clear();
		graph.adjustBreakNodes("[BREAK]");
//		if(!falseNotEmpty)
			graph.getOuts().add(control);
		

		return graph;
	}

	private CFG traverse_while(final CFGNode cfgNode, final Statement root) {
		this.isLoopPresent = true;
		final CFG graph = new CFG();
		final CFGNode control = new CFGNode("WHILE", NodeType.CONTROL, "WHILE", "WHILE");
		control.setAstNode(root);
		control.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getNodeId() + ".");
		graph.mergeSeq(control);

		final CFG branch = traverse(control, root.getStatements(0));
		branch.adjustBreakNodes("[CONTINUE]");
		graph.mergeABranch(branch, control, "T");
//		graph.addBackEdges(branch, control, "B");

		boolean trueNotEmpty = false, falseNotEmpty = false;

		if (root.getStatementsCount() > 1) { // Else
			this.isBranchPresent = true;
			final CFG falseBranch = traverse(control, root.getStatements(1));
			if (falseBranch.getNodes().size() > 0) {
				falseNotEmpty = true;
				graph.mergeABranch(falseBranch, control, "F");
			}
		}
		
		//graph.getOuts().clear();
		graph.adjustBreakNodes("[BREAK]");
		graph.getOuts().add(control);
		
//		if (falseNotEmpty)
//			graph.getOuts().remove(control);

		return graph;
	}

	private CFG traverse_do(final CFGNode cfgNode, final Statement root) {
		this.isLoopPresent = true;
		final CFG graph = new CFG();

		final CFGNode control = new CFGNode("DO", NodeType.CONTROL, "DO", "DO");
		control.setAstNode(root.getConditions(0));
		control.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getNodeId() + ".");
		graph.mergeSeq(control);

		final CFG branch = traverse(control, root.getStatements(0));
		branch.adjustBreakNodes("[CONTINUE]");
		graph.mergeABranch(branch, control, "T");
		graph.addBackEdges(branch, control, "B");

		graph.getOuts().clear();
		graph.adjustBreakNodes("[BREAK]");
		graph.getOuts().add(control);

		return graph;
	}

	private CFG traverse_labeled(final CFGNode cfgNode, final Statement root) {
		final CFG graph = traverse(cfgNode, root.getStatements(0));
		graph.adjustBreakNodes(root.getExpressions(0).getLiteral());
		return graph;
	}

	private CFG traverse_case(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		final String label;
		if (root.getKind() == StatementKind.DEFAULT) {
			label = "default";
		} else if (root.getExpressions(0).hasLiteral()) {
			label = root.getExpressions(0).getLiteral();
		} else {
			label = root.getExpressions(0).getVariable();
		}
		final CFGNode node = new CFGNode(label, NodeType.OTHER, "Case", label);
		node.setAstNode(root);
		graph.mergeSeq(node);
		graph.getOuts().add(node);
		return graph;
	}

	private CFG traverse_break(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		final String label;
		if (root.getExpressionsCount() > 0) {
			label = root.getExpressions(0).getLiteral();
		} else {
			label = "[BREAK]";
		}
		final CFGNode node = new CFGNode(label, NodeType.OTHER, "<GOTO>", label);
		node.setAstNode(root);
		graph.addBreakNode(node);
		return graph;
	}

	private CFG traverse_continue(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		final String label;
		if (root.getExpressionsCount() > 0) {
			label = root.getExpressions(0).getLiteral();
		} else {
			label = "[CONTINUE]";
		}
		final CFGNode node = new CFGNode(label, NodeType.OTHER, "<GOTO>", label);
		node.setAstNode(root);
		graph.addBreakNode(node);
		return graph;
	}

	private CFG traverse_throw(final CFGNode cfgNode, final Statement root) {
		this.isBranchPresent = true;
		final CFG graph = new CFG();
		// FIXME what if its "throw new m(a(), x());"?
		/*if (root.hasExpression())
			graph.mergeSeq(traverse(cfgNode, root.getExpression()));
		*/
//		for(int i=0;i< root.getExpressionsCount();i++)
//			graph.mergeSeq(traverse(cfgNode, root.getExpressions(i)));
		final CFGNode node = new CFGNode("END[throw]", NodeType.OTHER, "END[throw]", "END[throw]");
		node.setAstNode(root);
		graph.addReturnNode(node);
		return graph;
	}
	
	private CFG traverse_yield(final CFGNode cfgNode, final Expression root) {
		final CFG graph = new CFG();

		final CFGNode node = new CFGNode("END[yield]", NodeType.OTHER, "END[yield]", "END[yield]");
		node.setAstNode(root);
		if (root.getExpressionsCount() > 0)
			node.setAstNode(root.getExpressions(0));
		graph.addReturnNode(node);
		return graph;
	}
	
	private CFG traverse_return(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		// FIXME what if its "return m();"?
//		for(int i=0;i< root.getExpressionsCount();i++)
//			graph.mergeSeq(traverse(cfgNode, root.getExpressions(i)));
		
		final CFGNode node = new CFGNode("END[return]", NodeType.OTHER, "END[return]", "END[return]");
		node.setAstNode(root);
		if (root.getExpressionsCount() > 0)
			node.setAstNode(root.getExpressions(0));
		graph.addReturnNode(node);
		return graph;
	}

	private CFG traverse_with(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		final CFGNode branch = new CFGNode("WITH", NodeType.OTHER, "WITH", "WITH");
		branch.setAstNode(root);
		branch.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getNodeId() + ".");
		graph.mergeSeq(branch);

		boolean hasBody = false;
		if (root.getStatementsCount() > 0) {
			final CFG body = traverse(branch, root.getStatements(0));
			if (body.nodes.size() > 0) {
				graph.mergeABranch(body, branch, "T");
				hasBody = true;
			} else {
				graph.outs.add(branch);
			}
		}

		if (hasBody)
			graph.getOuts().remove(branch);

		return graph;
	}

	private CFG traverse_try(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		final CFGNode branch = new CFGNode("TRY", NodeType.CONTROL, "TRY", "TRY");
		branch.setAstNode(root);
		branch.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getNodeId() + ".");
		graph.mergeSeq(branch);

		boolean hasBody = false;
		if (root.getStatementsCount() > 0) {
			final CFG body = traverse(branch, root.getStatements(0));
			if (body.nodes.size() > 0) {
				graph.mergeABranch(body, branch, "T");
				hasBody = true;
			} else {
				graph.outs.add(branch);
			}
		}

		// all catch statements are considered false branches
		Statement finallyBlock = null;
		for (int i = 1; i < root.getStatementsCount(); i++) {
			final Statement stmt = root.getStatements(i);
			if (finallyBlock == null && stmt.getKind() == StatementKind.FINALLY) {
				finallyBlock = stmt;
			} else {
				graph.mergeABranch(traverse(branch, stmt), branch, "F");
			}
		}

		if (hasBody)
			graph.getOuts().remove(branch);

		if (finallyBlock != null) {
			graph.adjustReturnNodes();
			graph.mergeSeq(traverse(cfgNode, finallyBlock));
		}
		return graph;
	}

	private CFG traverse_catch(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		final CFGNode aNode = new CFGNode("CATCH", NodeType.OTHER, "CATCH", "CATCH");
		aNode.setAstNode(root);
		graph.mergeSeq(aNode);
		for (final Statement stmt : root.getStatementsList())
			graph.mergeSeq(traverse(cfgNode, stmt));
		return graph;
	}

	private CFG traverse_finally(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		final CFGNode aNode = new CFGNode("FINALLY", NodeType.OTHER, "FINALLY", "FINALLY");
		aNode.setAstNode(root);
		graph.mergeSeq(aNode);
		for (final Statement stmt : root.getStatementsList())
			graph.mergeSeq(traverse(cfgNode, stmt));
		return graph;
	}

	private CFG traverse_sync(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		final CFGNode aNode = new CFGNode();
		aNode.setAstNode(root.getExpressions(0));
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getNodeId() + ".");
		graph.mergeSeq(aNode);
		for (final Statement stmt : root.getStatementsList())
			graph.mergeSeq(traverse(aNode, stmt));
		return graph;
	}

	protected final void postorder(final CFGNode node, final java.util.Set<Integer> visitedNodes, final CFGNode[] results) throws Exception {
		results[visitedNodes.size()] = node;
		visitedNodes.add(node.getNodeId());
		for (final CFGNode succ : node.getSuccessors()) {
			if (!visitedNodes.contains(succ.getNodeId())) {
				postorder(succ, visitedNodes, results);
			}
		}
	}

	public CFGNode[] order() {
		try {
			final CFGNode[] results = new CFGNode[nodes.size()];
			final java.util.Set<Integer> visitedNodes = new java.util.HashSet<Integer>();
			postorder(this.getEntryNode(), visitedNodes, results);
			return results;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public CFGNode[] reverseSortNodes() {
		try {
			final CFGNode[] results = new CFGNode[nodes.size()];
			for (final CFGNode node : nodes) {
				results[nodes.size() - 1 - node.getNodeId()] = node;
			}
			return results;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public CFGNode[] sortNodes() {
		try {
			final CFGNode[] results = new CFGNode[nodes.size()];
			for (final CFGNode node : nodes) {
				results[node.getNodeId()] = node;
			}
			return results;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("ins: ");
		sb.append(ins.size());
		sb.append("\n");
		for (final CFGNode node : ins) {
			sb.append(node.toString());
			sb.append("\n");
		}
		sb.append("\n");
		sb.append("outs: ");
		sb.append(outs.size());
		sb.append("\n");
		for (final CFGNode node : outs) {
			sb.append(node.toString());
			sb.append("\n");
		}
		sb.append("\n");
		sb.append("breaks: ");
		sb.append(breaks.size());
		sb.append("\n");
		for (final CFGNode node : breaks) {
			sb.append(node.toString());
			sb.append("\n");
		}
		sb.append("\n");
		sb.append("returns: ");
		sb.append(returns.size());
		sb.append("\n");
		for (final CFGNode node : returns) {
			sb.append(node.toString());
			sb.append("\n");
		}
		sb.append("\n");
		sb.append("\n\nList of nodes:");
		sb.append(nodes.size());
		sb.append("\n");
		final CFGNode[] mynodes = nodes.toArray(new CFGNode[nodes.size()]);
		for (final CFGNode node : mynodes) {
			sb.append(node.toString());
			sb.append("\t");
			sb.append(node.getMethod());
			sb.append("\n");
		}
		int numEdges = 0;
		sb.append("\nList of edges:\n");
		for (final CFGNode node : mynodes) {
			for (final CFGEdge edge : node.getOutEdges()) {
				sb.append(edge.toString());
				sb.append("\n");
				numEdges++;
			}
		}
		sb.append("Total ");
		sb.append(numEdges);
		sb.append(" edges\n");
		return sb.toString();
	}

	public Builder newBuilder() {
		final CFGNode[] sortedNodes = sortNodes();
		final int size = sortedNodes.length;

		final Builder b = boa.types.Control.Graph.newBuilder();

		for (final CFGNode n : sortedNodes) {
			b.addNodes(n.newBuilder().build());
		}

		final Map<Integer, EdgeLabel> edgeLabels = new HashMap<Integer, EdgeLabel>();
		for (final CFGNode node : sortedNodes) {
			for (final CFGEdge edge : node.getOutEdges()) {
				final CFGNode dest = edge.getDest();
				final int index = node.getNodeId() * size + dest.getNodeId();
				edgeLabels.put(index, CFGEdge.convertLabel(edge.getLabel()));
			}
		}

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				final int index = i * size + j;
				final boa.types.Control.Edge.Builder eb = boa.types.Control.Edge.newBuilder();
				if (edgeLabels.containsKey(index))
					eb.setLabel(edgeLabels.get(index));
				else
					eb.setLabel(boa.types.Control.Edge.EdgeLabel.NIL);
				b.addEdges(eb.build());
			}
		}
		return b;
	}
}
