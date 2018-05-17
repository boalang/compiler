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
import java.util.List;
import java.util.Map;

import boa.functions.BoaAstIntrinsics;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Statement;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Variable;
import boa.types.Control.CFG.Builder;
import boa.types.Control.CFGEdge.CFGEdgeLabel;
import boa.types.Control.CFGNode.CFGNodeType;

/**
 * Control flow graph builder.
 *
 * @author ganeshau
 * @author rdyer
 * @author marafat
 */
public class CFG {
	public Method md;
	public String class_name;

	protected final HashSet<CFGNode> nodes = new HashSet<CFGNode>();
	private CFGNode entryNode;
	private CFGNode exitNode;

	private final HashSet<CFGNode> outs = new HashSet<CFGNode>();
	private final HashSet<CFGNode> ins = new HashSet<CFGNode>();
	private final HashSet<CFGNode> breaks = new HashSet<CFGNode>();
	private final HashSet<CFGNode> returns = new HashSet<CFGNode>();

	private boolean isLoopPresent = false;
	private boolean isBranchPresent = false;
	private boolean paramAsStatement = false;

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

	public void setIsLoopPresent(final boolean isLoopPresent) {
		this.isLoopPresent = isLoopPresent;
	}

	public void setIsBranchPresent(final boolean isBranchPresent) {
		this.isBranchPresent = isBranchPresent;
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
			if (node.getId() == id)
				return node;
		}
		return null;
	}

	public void addNode(final CFGNode node) {
		if (nodes.contains(node))
			return;
		outs.add(node);
		nodes.add(node);
		ins.add(node);
	}

	public void removeNode(final CFGNode node) {
		if (!nodes.contains(node))
			return;
		nodes.remove(node);
		ins.remove(node);
		outs.remove(node);
	}

	/*
	 * merge two graph together merge outs with target's ins update outs and ins
	 */
	public void mergeSeq(final CFG target) {
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

	public void createNewEdge(final CFGNode src, final CFGNode dest) {
		createNewEdge(src, dest, null);
	}

	public void createNewEdge(final CFGNode src, final CFGNode dest, final String label) {
		if (src.getOutNodes().contains(dest))
			return;

		if (label == null)
			new CFGEdge(src, dest);
		else
			new CFGEdge(src, dest, label);
	}

	public void mergeSeq(final CFGNode branch) {
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

	public void mergeBranches(final CFG target, final HashSet<CFGNode> saveOuts) {
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

	public void mergeABranch(final CFG target, final CFGNode branch) {
		mergeABranch(target, branch, null);
	}

	public void mergeABranch(final CFG target, final CFGNode branch, final String label) {
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

	public void addBackEdges(final CFG target, final CFGNode branch, final String label) {
		for (final CFGNode aNode : target.outs) {
			createNewEdge(aNode, branch, label);
		}
	}

	public void addBreakNode(final String identifier) {
		addBreakNode(new CFGNode(identifier, CFGNodeType.OTHER, "<GOTO>", identifier));
	}

	public void addBreakNode(final CFGNode node) {
		this.mergeSeq(node);
		this.outs.remove(node);
		this.breaks.add(node);
	}

	public void addReturnNode() {
		addReturnNode(new CFGNode("END[return]", CFGNodeType.OTHER, "END[return]", "END[return]"));
	}

	public void addReturnNode(final CFGNode node) {
		this.mergeSeq(node);
		this.outs.remove(node);
		this.returns.add(node);
	}

	public void adjustBreakNodes(final String id) {
		for (final CFGNode node : new ArrayList<CFGNode>(this.breaks)) {
			if (node.getObjectName().equals(id)) {
				this.outs.add(node);
				this.breaks.remove(node);
			}
		}
	}

	public void adjustReturnNodes() {
		this.outs.addAll(this.returns);
		this.returns.clear();
	}

	public Statement getStatement() {
		final Statement.Builder stm = Statement.newBuilder();
		stm.setKind(StatementKind.BLOCK);

		for (final Variable v : md.getArgumentsList()) { //FIXME assign proper type values
			final Expression exp = BoaAstIntrinsics.parseexpression(v.getName() + "= 1");
			final Statement.Builder stm1 = Statement.newBuilder();
			stm1.setKind(StatementKind.EXPRESSION);
			stm1.setExpression(exp);
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
			final CFGNode startNode = new CFGNode("ENTRY", CFGNodeType.ENTRY, "ENTRY", "ENTRY");
			mergeSeq(startNode);
			if (paramAsStatement)
				mergeSeq(traverse(startNode, getStatement()));
			else
				mergeSeq(traverse(startNode, md.getStatements(0)));

			adjustReturnNodes();
			final CFGNode endNode = new CFGNode("EXIT", CFGNodeType.ENTRY, "EXIT", "EXIT");
			mergeSeq(endNode);
			this.exitNode = endNode;

			this.entryNode = startNode;
			for (final CFGNode node : this.nodes) {
				if (node.hasExpr()) {
					if (node.getExpr().getKind() == ExpressionKind.VARDECL) {
						if (node.getExpr().getVariableDecls(0).hasInitializer()) {
							node.setRhs(node.getExpr().getVariableDecls(0).getInitializer());
						}
					} else if (node.getExpr().getKind() == ExpressionKind.ASSIGN) {
						node.setRhs(node.getExpr().getExpressions(1));
					}
				}
				node.setDefVariables(node.processDef());
				node.setUseVariables(node.processUse());
				node.setSuccessors(node.getOutNodes());
				node.setPredecessors(node.getInNodes());
			}

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
		default:
			/*
			 * final List<Expression> expressionsList =
			 * root.getExpressionsList(); final int expressionsSize =
			 * expressionsList.size(); for (int i = 0; i < expressionsSize; i++)
			 * { graph.mergeSeq(traverse(cfgNode, expressionsList.get(i))); }
			 */
			final CFGNode bNode = new CFGNode(root.getKind().name(), CFGNodeType.OTHER, "", root.getKind().name());
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
			return traverse(cfgNode, root.getExpression());
		case SYNCHRONIZED:
			return traverse_sync(cfgNode, root);
		case RETURN:
			return traverse_return(cfgNode, root);
		case FOR:
			return traverse_for(cfgNode, root);
		case DO:
			return traverse_do(cfgNode, root);
		case WHILE:
			return traverse_while(cfgNode, root);
		case IF:
			return traverse_if(cfgNode, root);
		case BREAK:
			return traverse_break(cfgNode, root);
		case CASE:
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
			return traverse_throw(cfgNode, root);
		case CATCH:
			return traverse_catch(cfgNode, root);
		case ASSERT:
		case TYPEDECL:
		case OTHER:
			final CFGNode aNode = new CFGNode(root.getKind().name(), CFGNodeType.OTHER, "", root.getKind().name());
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

		final CFGNode aNode = new CFGNode("$new$", CFGNodeType.METHOD, type_str, nObjectname, root.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_clinit(final CFGNode cfgNode, final Expression root) {
		final CFG graph = new CFG();
		final CFGNode aNode = new CFGNode("<init>", CFGNodeType.METHOD, class_name, "super", root.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_init(final CFGNode cfgNode, final Expression root) {
		final CFG graph = new CFG();
		final CFGNode aNode = new CFGNode("<init>", CFGNodeType.METHOD, class_name, "this", root.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_call(final CFGNode cfgNode, final Expression root) {
		final CFG graph = new CFG();
		final CFGNode aNode = new CFGNode(root.getMethod(), CFGNodeType.METHOD, class_name, "this", root.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_super(final CFGNode cfgNode, final Expression root) {
		final CFG graph = new CFG();
		final CFGNode aNode = new CFGNode(root.getMethod(), CFGNodeType.METHOD, class_name, "super", root.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_if (final CFGNode cfgNode, final Statement root) {
		this.isBranchPresent = true;
		final CFG graph = new CFG();

		final CFGNode branch = new CFGNode("IF", CFGNodeType.CONTROL, "IF", "IF");
		branch.setAstNode(root.getExpression());
		branch.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
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

		final CFGNode branch = new CFGNode("IF", CFGNodeType.CONTROL, "IF", "IF");
		branch.setAstNode(root);
		branch.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
		graph.mergeSeq(branch);

		boolean trueNotEmpty = false, falseNotEmpty = false;
		if (root.getExpressionsCount() > 0) { // Then
			final CFG trueBranch = traverse(branch, root.getExpressions(0));
			if (trueBranch.getNodes().size() > 0) {
				graph.mergeABranch(trueBranch, branch, "T");
				trueNotEmpty = true;
			}
		}
		if (root.getExpressionsCount() > 1) { // Else
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
		final CFGNode node = new CFGNode(root.getExpression().toString(), CFGNodeType.CONTROL, "SWITCH", "SWITCH");
		node.setAstNode(root.getExpression());
		node.setPid((cfgNode == null) ? "." : cfgNode.getPid()+ cfgNode.getId() + ".");
		graph.mergeSeq(node);
		CFG subgraph = null;
		Statement sc = null;
		boolean hasdefault = false;
		for (final Statement s : root.getStatementsList()) {
			if (s.getKind() == StatementKind.CASE) {
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
					if (sc.hasExpression())
						graph.mergeABranch(lastsub, node, sc.getExpression().toString());
					else
						graph.mergeABranch(lastsub, node);
				}
				sc = s;
				if (!s.hasExpression())
					hasdefault = true;
			} else {
				subgraph.mergeSeq(traverse(node, s));
			}
		}
		if (subgraph != null) {
			if (sc.hasExpression())
				graph.mergeABranch(subgraph, node, sc.getExpression().toString());
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
			final CFGNode bNode = new CFGNode("VARDECL", CFGNodeType.OTHER, "", "VARDECL");
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
		final CFGNode control = new CFGNode("FOR", CFGNodeType.CONTROL, "FOR", "FOR");
		control.setAstNode(root.getExpression());
		control.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
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
		graph.addBackEdges(body, control, "B");

		graph.getOuts().clear();
		graph.adjustBreakNodes("[BREAK]");
		graph.getOuts().add(control);

		return graph;
	}

	private CFG traverse_while(final CFGNode cfgNode, final Statement root) {
		this.isLoopPresent = true;
		final CFG graph = new CFG();
		final CFGNode control = new CFGNode("WHILE", CFGNodeType.CONTROL, "WHILE", "WHILE");
		control.setAstNode(root.getExpression());
		control.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
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

	private CFG traverse_do(final CFGNode cfgNode, final Statement root) {
		this.isLoopPresent = true;
		final CFG graph = new CFG();

		final CFGNode control = new CFGNode("DO", CFGNodeType.CONTROL, "DO", "DO");
		control.setAstNode(root.getExpression());
		control.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
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
		graph.adjustBreakNodes(root.getExpression().getLiteral());
		return graph;
	}

	private CFG traverse_case(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		final String label;
		if (!root.hasExpression()) {
			label = "default";
		} else if (root.getExpression().hasLiteral()) {
			label = root.getExpression().getLiteral();
		} else {
			label = root.getExpression().getVariable();
		}
		final CFGNode node = new CFGNode(label, CFGNodeType.OTHER, "Case", label);
		node.setAstNode(root);
		graph.mergeSeq(node);
		graph.getOuts().add(node);
		return graph;
	}

	private CFG traverse_break(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		final String label;
		if (root.hasExpression()) {
			label = root.getExpression().getLiteral();
		} else {
			label = "[BREAK]";
		}
		final CFGNode node = new CFGNode(label, CFGNodeType.OTHER, "<GOTO>", label);
		node.setAstNode(root);
		graph.addBreakNode(node);
		return graph;
	}

	private CFG traverse_continue(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		final String label;
		if (root.hasExpression()) {
			label = root.getExpression().getLiteral();
		} else {
			label = "[CONTINUE]";
		}
		final CFGNode node = new CFGNode(label, CFGNodeType.OTHER, "<GOTO>", label);
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
		final CFGNode node = new CFGNode("END[throw]", CFGNodeType.OTHER, "END[throw]", "END[throw]");
		node.setAstNode(root);
		graph.addReturnNode(node);
		return graph;
	}

	private CFG traverse_return(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		// FIXME what if its "return m();"?
		/*if (root.hasExpression())
			graph.mergeSeq(traverse(cfgNode, root.getExpression()));
		*/
		final CFGNode node = new CFGNode("END[return]", CFGNodeType.OTHER, "END[return]", "END[return]");
		node.setAstNode(root);
		graph.addReturnNode(node);
		return graph;
	}

	private CFG traverse_try(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		final CFGNode branch = new CFGNode("TRY", CFGNodeType.CONTROL, "TRY", "TRY");
		branch.setAstNode(root);
		branch.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
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
			if (finallyBlock == null && stmt.getKind() == StatementKind.BLOCK) {
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
		final CFGNode aNode = new CFGNode("CATCH", CFGNodeType.OTHER, "CATCH", "CATCH");
		aNode.setAstNode(root);
		graph.mergeSeq(aNode);
		for (final Statement stmt : root.getStatementsList())
			graph.mergeSeq(traverse(cfgNode, stmt));
		return graph;
	}

	private CFG traverse_sync(final CFGNode cfgNode, final Statement root) {
		final CFG graph = new CFG();
		final CFGNode aNode = new CFGNode();
		aNode.setAstNode(root.getExpression());
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid() + cfgNode.getId() + ".");
		graph.mergeSeq(aNode);
		for (final Statement stmt : root.getStatementsList())
			graph.mergeSeq(traverse(aNode, stmt));
		return graph;
	}

	public final void postorder(final CFGNode node, final java.util.Set<Integer> visitedNodes, final CFGNode[] results) throws Exception {
		results[visitedNodes.size()] = node;
		visitedNodes.add(node.getId());
		for (final CFGNode succ : node.getSuccessorsList()) {
			if (!visitedNodes.contains(succ.getId())) {
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

	public CFGNode[] nodes() {
		try {
			final CFGNode[] results = new CFGNode[nodes.size()];
			int i = 0;
			for (final CFGNode node : nodes) {
				results[i++] = node;
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
				results[node.getId()] = node;
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
		final CFGNode[] mynodes = nodes();
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
		final int size = nodes.size();
		final Builder b = boa.types.Control.CFG.newBuilder();

		for (final CFGNode n : nodes) {
			b.addNodes(n.newBuilder());
		}

		final CFGNode[] sortedNodes = sortNodes();
		final Map<Integer, CFGEdgeLabel> edgeLabels = new HashMap<Integer, CFGEdgeLabel>();
		for (final CFGNode node : sortedNodes) {
			for (final CFGEdge edge : node.getOutEdges()) {
				final CFGNode anoNode = edge.getDest();
				final int index = node.getId() * size + anoNode.getId();
				edgeLabels.put(index, CFGEdge.getLabel(edge.label()));
			}
		}

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				final int index = i * size + j;
				final boa.types.Control.CFGEdge.Builder eb = boa.types.Control.CFGEdge.newBuilder();
				if (edgeLabels.containsKey(index))
					eb.setLabel(edgeLabels.get(index));
				else
					eb.setLabel(boa.types.Control.CFGEdge.CFGEdgeLabel.NIL);
				b.addEdges(eb.build());
			}
		}
		return b;
	}
}
