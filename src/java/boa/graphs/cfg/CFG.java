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
package boa.graphs.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Statement;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Type;
import boa.types.Ast.Variable;
import boa.types.Control.CFG.Builder;
import boa.types.Control.CFGEdge.CFGEdgeLabel;

/**
 * Control flow graph builder
 * @author ganeshau
 *
 */
public class CFG {
	public static final int minSize = -1;

	public Method md;
	public String class_name;

	protected HashSet<CFGNode> nodes = new HashSet<CFGNode>();
	private HashSet<CFGNode> outs = new HashSet<CFGNode>();
	private HashSet<CFGNode> ins = new HashSet<CFGNode>();
	private HashSet<CFGNode> breaks = new HashSet<CFGNode>();
	private HashSet<CFGNode> returns = new HashSet<CFGNode>();

	public CFG(Method method) {
		this.md = method;
		this.class_name = "this";
	}

	public CFG(Method method, String cls_name) {
		this.md = method;
		this.class_name = cls_name;
	}

	public CFG() {
		// TODO Auto-generated constructor stub
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

	public void addNode(CFGNode node) {
		if (!nodes.contains(node)) {
			outs.add(node);
			nodes.add(node);
			ins.add(node);
		}
	}

	public void removeNode(CFGNode node) {
		if (nodes.contains(node)) {
			nodes.remove(node);
			ins.remove(node);
			outs.remove(node);
		}
	}

	/*
	 * merge two graph together merge outs with target's ins update outs and ins
	 */
	public void mergeSeq(CFG target) {
		// merge Node
		if (target.getNodes().size() == 0)
			return;

		if (nodes.size() == 0) {
			// add Nodes
			nodes.addAll(target.getNodes());
			// merge Edges
			ins.addAll(target.ins);
			outs.addAll(target.outs);
			breaks.addAll(target.breaks);
			returns.addAll(target.returns);
			return;
		}

		// add Nodes
		nodes.addAll(target.getNodes());
		// merge Edges

		for (CFGNode aNode : outs) {
			for (CFGNode anoNode : target.ins) {
				createNewEdge(aNode, anoNode);
			}
		}

		// keep only outs of right most child iff the child has outs
		/* if (target.getOuts().size() != 0) */{
			outs.clear();
			outs.addAll(target.outs);
		}
		breaks.addAll(target.breaks);
		returns.addAll(target.returns);
	}

	public void createNewEdge(CFGNode node, CFGNode anoNode) {
		if (node.getClassName() == null) {
			new CFGEdge(node, anoNode);
			return;
		}

		new CFGEdge(node, anoNode);
	}

	public void createNewEdge(CFGNode node, CFGNode anoNode, String label) {
		if (!node.getOutNodes().contains(anoNode))
			new CFGEdge(node, anoNode, label);
	}

	public void mergeSeq(CFGNode branch) {
		this.addNode(branch);
		/*
		 * branch will not be considered ins node except the input graph is size
		 * 0
		 */
		ins.remove(branch);
		for (CFGNode aNode : outs) {
			if (!aNode.equals(branch)) {
				createNewEdge(aNode, branch);
			}
		}

		if (ins.size() == 0)
			ins.add(branch);
		outs.clear();
		outs.add(branch);
	}

	public void mergeBranches(CFG target, HashSet<CFGNode> saveOuts) {
		if (target.getNodes().size() == 0)
			return;

		if (saveOuts.size() == 0) {
			// add Nodes
			nodes.addAll(target.getNodes());
			// merge Edges

			ins.addAll(target.getIns());
			outs.addAll(target.outs);
			return;
		}

		// add Nodes
		nodes.addAll(target.getNodes());
		// merge Edges

		for (CFGNode aNode : saveOuts) {
			for (CFGNode anoNode : target.ins) {
				createNewEdge(aNode, anoNode);
			}
		}
		outs.addAll(target.outs);
	}

	public void mergeABranch(CFG target, CFGNode branch) {
		// merge Node
		if (target.getNodes().size() == 0)
			return;

		// add Nodes
		nodes.addAll(target.getNodes());
		// merge Edges
		for (CFGNode aNode : target.ins) {
			createNewEdge(branch, aNode);
		}
		// keep all outs node of children
		outs.addAll(target.outs);
		breaks.addAll(target.breaks);
		returns.addAll(target.returns);
	}

	public void mergeABranch(CFG target, CFGNode branch, String label) {
		// merge Node
		if (target.getNodes().size() == 0)
			return;

		// add Nodes
		nodes.addAll(target.getNodes());
		// merge Edges
		for (CFGNode aNode : target.ins) {
			createNewEdge(branch, aNode, label);
		}
		// keep all outs node of children
		outs.addAll(target.outs);
		breaks.addAll(target.breaks);
		returns.addAll(target.returns);
	}

	public void addBackEdges(CFG target, CFGNode branch, String label) {
		for (CFGNode aNode : target.outs) {
			createNewEdge(aNode, branch, label);
		}
	}

	public void addBreakNode(String identifier) {
		CFGNode node = new CFGNode(identifier, CFGNode.TYPE_OTHER, "<GOTO>",
				identifier);
		this.mergeSeq(node);
		this.outs.remove(node);
		this.breaks.add(node);
	}

	public void addBreakNode(CFGNode node) {
		this.mergeSeq(node);
		this.outs.remove(node);
		this.breaks.add(node);
	}

	public void addReturnNode() {
		CFGNode node = new CFGNode("END", CFGNode.TYPE_OTHER, "<GOTO>", "END");
		this.mergeSeq(node);
		this.outs.remove(node);
		this.returns.add(node);
	}

	public void addReturnNode(CFGNode node) {
		this.mergeSeq(node);
		this.outs.remove(node);
		this.returns.add(node);
	}

	public void adjustBreakNodes(String id) {
		for (CFGNode node : new HashSet<CFGNode>(this.breaks)) {
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

	public boolean isEmpty() {
		return this.nodes.isEmpty();
	}

	public void astToCFG() {
		if (md.getStatementsCount() > 0) {
			CFGNode startNode = new CFGNode("START", CFGNode.TYPE_ENTRY,
					"START", "START");
			mergeSeq(startNode);
			mergeSeq(traverse(startNode, md.getStatementsList().get(0)));
			if (getNodes().size() >= CFG.minSize) {
				adjustReturnNodes();
				CFGNode endNode = new CFGNode("END", CFGNode.TYPE_ENTRY, "END",
						"END");
				mergeSeq(endNode);
			}
		}
	}

	private CFG traverse(CFGNode cfgNode, Expression root) {
		CFG graph = new CFG();
		switch (root.getKind().getNumber()) {
		case ExpressionKind.CONDITIONAL_VALUE:
			return traverse_conditional(cfgNode, root);
		case ExpressionKind.METHODCALL_VALUE:
			if (root.getMethod().equals("<init>"))
				return traverse_init(cfgNode, root);
			else if (root.getMethod().equals("super"))
				return traverse_clinit(cfgNode, root);
			else if (root.getMethod().contains("super."))
				return traverse_super(cfgNode, root);
			else
				return traverse_call(cfgNode, root);
		case ExpressionKind.NEW_VALUE:
			return traverse_instance(cfgNode, root);
		case ExpressionKind.NEQ_VALUE:
		case ExpressionKind.EQ_VALUE:
		case ExpressionKind.GT_VALUE:
		case ExpressionKind.GTEQ_VALUE:
		case ExpressionKind.LITERAL_VALUE:
		case ExpressionKind.LOGICAL_AND_VALUE:
		case ExpressionKind.LOGICAL_NOT_VALUE:
		case ExpressionKind.LOGICAL_OR_VALUE:
		case ExpressionKind.LT_VALUE:
		case ExpressionKind.LTEQ_VALUE:
		case ExpressionKind.ANNOTATION_VALUE:
		case ExpressionKind.ARRAYINDEX_VALUE:
		case ExpressionKind.ARRAYINIT_VALUE:
		case ExpressionKind.ASSIGN_ADD_VALUE:
		case ExpressionKind.ASSIGN_BITAND_VALUE:
		case ExpressionKind.ASSIGN_BITOR_VALUE:
		case ExpressionKind.ASSIGN_BITXOR_VALUE:
		case ExpressionKind.ASSIGN_DIV_VALUE:
		case ExpressionKind.ASSIGN_LSHIFT_VALUE:
		case ExpressionKind.ASSIGN_MOD_VALUE:
		case ExpressionKind.ASSIGN_MULT_VALUE:
		case ExpressionKind.ASSIGN_RSHIFT_VALUE:
		case ExpressionKind.ASSIGN_SUB_VALUE:
		case ExpressionKind.ASSIGN_UNSIGNEDRSHIFT_VALUE:
		case ExpressionKind.ASSIGN_VALUE:
		case ExpressionKind.BIT_AND_VALUE:
		case ExpressionKind.BIT_LSHIFT_VALUE:
		case ExpressionKind.BIT_NOT_VALUE:
		case ExpressionKind.BIT_OR_VALUE:
		case ExpressionKind.BIT_RSHIFT_VALUE:
		case ExpressionKind.BIT_UNSIGNEDRSHIFT_VALUE:
		case ExpressionKind.BIT_XOR_VALUE:
		case ExpressionKind.CAST_VALUE:
		case ExpressionKind.NEWARRAY_VALUE:
		case ExpressionKind.NULLCOALESCE_VALUE:
		case ExpressionKind.OP_ADD_VALUE:
		case ExpressionKind.OP_DEC_VALUE:
		case ExpressionKind.OP_DIV_VALUE:
		case ExpressionKind.OP_INC_VALUE:
		case ExpressionKind.OP_MOD_VALUE:
		case ExpressionKind.OP_MULT_VALUE:
		case ExpressionKind.OP_SUB_VALUE:
		case ExpressionKind.OTHER_VALUE:
		case ExpressionKind.TYPECOMPARE_VALUE:
		case ExpressionKind.VARACCESS_VALUE:
		case ExpressionKind.VARDECL_VALUE:
			/*
			 * final List<Expression> expressionsList =
			 * root.getExpressionsList(); final int expressionsSize =
			 * expressionsList.size(); for (int i = 0; i < expressionsSize; i++)
			 * { graph.mergeSeq(traverse(cfgNode, expressionsList.get(i))); }
			 */
			CFGNode bNode = new CFGNode(root.getKind().name(),
					CFGNode.TYPE_OTHER, "", root.getKind().name());
			bNode.setAstNode(root);
			graph.mergeSeq(bNode);
			return graph;
		}
		return graph;
	}

	private CFG traverse(CFGNode cfgNode, Statement root) {
		CFG graph = new CFG();
		switch (root.getKind().getNumber()) {
		case StatementKind.BLOCK_VALUE:
			final List<Statement> statementsList = root.getStatementsList();
			final int statementsSize = statementsList.size();
			for (int i = 0; i < statementsSize; i++)
				graph.mergeSeq(traverse(cfgNode, statementsList.get(i)));
			break;
		case StatementKind.EXPRESSION_VALUE:
			return traverse(cfgNode, root.getExpression());
		case StatementKind.SYNCHRONIZED_VALUE:
			return traverse_sync(cfgNode, root);
		case StatementKind.RETURN_VALUE:
			return traverse_return(cfgNode, root);
		case StatementKind.FOR_VALUE:
			return traverse_for(cfgNode, root);
		case StatementKind.DO_VALUE:
			return traverse_do(cfgNode, root);
		case StatementKind.WHILE_VALUE:
			return traverse_while(cfgNode, root);
		case StatementKind.IF_VALUE:
			return traverse_if(cfgNode, root);
		case StatementKind.BREAK_VALUE:
			return traverse_break(cfgNode, root);
		case StatementKind.CONTINUE_VALUE:
			return traverse_continue(cfgNode, root);
		case StatementKind.LABEL_VALUE:
			return traverse_labeled(cfgNode, root);
		case StatementKind.SWITCH_VALUE:
			return traverse_switch(cfgNode, root);
			/*
			 * case StatementKind.CASE_VALUE: return traverse_;
			 */
		case StatementKind.TRY_VALUE:
			return traverse_try(cfgNode, root);
		case StatementKind.THROW_VALUE:
			return traverse_throw(cfgNode, root);
		case StatementKind.CATCH_VALUE:
			return traverse_catch(cfgNode, root);
			/*
			 * case StatementKind.EMPTY_VALUE: break;
			 */
		case StatementKind.ASSERT_VALUE:
		case StatementKind.TYPEDECL_VALUE:
		case StatementKind.OTHER_VALUE:
			CFGNode aNode = new CFGNode(root.getKind().name(),
					CFGNode.TYPE_OTHER, "", root.getKind().name());
			aNode.setAstNode(root);
			graph.mergeSeq(aNode);
			return graph;
		default:
			System.out.println("[Error] " + root.getKind().name());
			break;
		}
		return graph;
	}

	private CFG traverse_instance(CFGNode cfgNode, Expression root) {
		CFG graph = new CFG();
		String nObjectname;
		if (root.getVariable() != null && !root.getVariable().equals("")) {
			Variable pNode = root.getVariableDecls(0); // TODO:check for
														// multiple var_decls
			nObjectname = pNode.getName().toString();
		} else if ((root.getExpressionsCount() > 0)
				&& root.getExpressions(0).getKind() == ExpressionKind.ASSIGN) {
			Expression ex = root.getExpressions(0);
			if (ex.getKind() == ExpressionKind.VARACCESS) {
				nObjectname = ex.getVariable();
			} else
				nObjectname = ex.toString();
		} else {
			nObjectname = "anonymous";
		}

		Type curr_type = null;
		String type_str = "";
		if (root.getVariableDeclsCount() > 0) {
			curr_type = root.getVariableDecls(0).getVariableType();// root.getProperty("TypeBinding");
			type_str = String.valueOf(curr_type.getName());
		} else if (root.getNewType() != null) {
			type_str = String.valueOf(root.getNewType().getName());
		}

		CFGNode aNode = new CFGNode("$new$", CFGNode.TYPE_METHOD, type_str,
				nObjectname, root.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid()
				+ cfgNode.getId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_clinit(CFGNode cfgNode, Expression root) {
		CFG graph = new CFG();
		CFGNode aNode = new CFGNode("<init>", CFGNode.TYPE_METHOD, class_name,
				"super", root.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid()
				+ cfgNode.getId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_init(CFGNode cfgNode, Expression root) {
		CFG graph = new CFG();
		CFGNode aNode = new CFGNode("<init>", CFGNode.TYPE_METHOD, class_name,
				"this", root.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid()
				+ cfgNode.getId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_call(CFGNode cfgNode, Expression root) {
		CFG graph = new CFG();
		String type_str;
		if (root.getExpressionsCount() == 0) {
			type_str = class_name;
		} else {
			type_str = "<>";
		}
		CFGNode aNode = null;
		aNode = new CFGNode(root.getMethod(), CFGNode.TYPE_METHOD, class_name,
				"this", root.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid()
				+ cfgNode.getId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_super(CFGNode cfgNode, Expression root) {
		CFG graph = new CFG();
		CFGNode aNode = null;
		Expression mcall = root;
		aNode = new CFGNode(mcall.getMethod(), CFGNode.TYPE_METHOD, class_name,
				"super", mcall.getExpressionsCount());
		aNode.setAstNode(root);
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid()
				+ cfgNode.getId() + ".");
		graph.mergeSeq(aNode);
		return graph;
	}

	private CFG traverse_if(CFGNode cfgNode, Statement root) {
		CFG graph = new CFG();
		/*
		 * assumption node 0 is conditional node
		 */
		if (root.getExpression() != null) {
			graph.mergeSeq(traverse(cfgNode, root.getExpression()));
		}

		CFGNode branch = new CFGNode("IF", CFGNode.TYPE_CONTROL, "IF", "IF");
		branch.setAstNode(root.getExpression());
		branch.setPid((cfgNode == null) ? "." : cfgNode.getPid()
				+ cfgNode.getId() + ".");
		graph.mergeSeq(branch);

		boolean trueNotEmpty = false, falseNotEmpty = false;
		if (root.getStatementsCount() > 0) { // Then
			CFG trueBranch = traverse(branch, root.getStatements(0));
			graph.mergeABranch(trueBranch, branch, "T");
			trueNotEmpty = true;
		}
		if (root.getStatementsCount() > 1) { // Else
			CFG falseBranch = traverse(branch, root.getStatements(1));
			graph.mergeABranch(falseBranch, branch, "F");
			falseNotEmpty = true;
		}
		if (trueNotEmpty && falseNotEmpty)
			graph.getOuts().remove(branch);
		return graph;
	}

	private CFG traverse_conditional(CFGNode cfgNode, Expression root) {
		CFG graph = new CFG();
		/*
		 * assumption node 0 is conditional node
		 */
		if (root.getExpressionsCount() == 3) {
			graph.mergeSeq(traverse(cfgNode, root.getExpressions(0)));
		}

		CFGNode branch = new CFGNode("IF", CFGNode.TYPE_CONTROL, "IF", "IF");
		branch.setAstNode(root);
		branch.setPid((cfgNode == null) ? "." : cfgNode.getPid()
				+ cfgNode.getId() + ".");
		graph.mergeSeq(branch); // TODO: check if the expressions index is right

		boolean trueNotEmpty = false, falseNotEmpty = false;
		if (root.getExpressionsCount() > 0) { // Then
			CFG trueBranch = traverse(branch, root.getExpressions(0));
			graph.mergeABranch(trueBranch, branch, "T");
			trueNotEmpty = true;
		}
		if (root.getExpressionsCount() > 1) { // Else
			CFG falseBranch = traverse(branch, root.getExpressions(1));
			graph.mergeABranch(falseBranch, branch, "F");
			falseNotEmpty = true;
		}
		if (trueNotEmpty && falseNotEmpty)
			graph.getOuts().remove(branch);
		return graph;
	}

	private CFG traverse_switch(CFGNode cfgNode, Statement root) {
		CFG graph = new CFG();
		graph.mergeSeq(traverse(cfgNode, root.getExpression()));
		CFGNode node = new CFGNode(root.getExpression().toString(),
				CFGNode.TYPE_CONTROL, "SWITCH", "SWITCH");
		node.setAstNode(root.getExpression());
		node.setPid((cfgNode == null) ? "." : cfgNode.getPid()
				+ cfgNode.getId() + ".");
		graph.mergeSeq(node);
		CFG subgraph = null;
		Statement sc = null;
		for (int i = 0; i < root.getStatementsCount(); i++) {
			Statement s = root.getStatements(i);
			if (s.getKind() == StatementKind.CASE) {
				if (subgraph != null) {
					if (sc.getExpression() != null)
						graph.mergeABranch(subgraph, node, sc.getExpression()
								.toString());
					else
						graph.mergeABranch(subgraph, node);
					if ((sc.getExpression() == null) && !subgraph.isEmpty())
						graph.getOuts().remove(node);
				}
				subgraph = new CFG();
				sc = s;
				subgraph.mergeSeq(traverse(node, s.getExpression()));
			} else
				subgraph.mergeSeq(traverse(node, s));
		}
		if (subgraph != null) {
			if (sc.getExpression() != null)
				graph.mergeABranch(subgraph, node, sc.getExpression()
						.toString());
			else
				graph.mergeABranch(subgraph, node);
		}
		if (sc != null && (sc.getExpression() == null) && !subgraph.isEmpty())
			graph.getOuts().remove(node);
		graph.adjustBreakNodes("");
		return graph;
	}

	private CFG traverse_for(CFGNode cfgNode, Statement root) {
		CFG graph = new CFG();
		for (Iterator it = root.getInitializationsList().iterator(); it
				.hasNext();) {
			Expression e = (Expression) it.next();
			graph.mergeSeq(traverse(cfgNode, e));
		}
		if (root.getInitializationsCount() == 0) { // enhanced for
			Expression e = root.getVariableDeclaration().getInitializer();
			if (e != null)
				graph.mergeSeq(traverse(cfgNode, e));
		}
		if (root.getExpression() != null) {
			graph.mergeSeq(traverse(cfgNode, root.getExpression()));
		}

		CFGNode control = new CFGNode("FOR", CFGNode.TYPE_CONTROL, "FOR", "FOR");
		control.setAstNode(root.getExpression());
		control.setPid((cfgNode == null) ? "." : cfgNode.getPid()
				+ cfgNode.getId() + ".");

		graph.mergeSeq(control);
		CFG branch = traverse(control, root.getStatements(0));
		graph.mergeABranch(branch, control, "T");
		graph.addBackEdges(branch, control, "B");

		graph.adjustBreakNodes("");
		return graph;
	}

	private CFG traverse_while(CFGNode cfgNode, Statement root) {
		CFG graph = new CFG();
		if (root.getExpression() != null) {
			graph.mergeSeq(traverse(cfgNode, root.getExpression()));
		}

		CFGNode control = new CFGNode("WHILE", CFGNode.TYPE_CONTROL, "WHILE",
				"WHILE");
		control.setAstNode(root.getExpression());
		control.setPid((cfgNode == null) ? "." : cfgNode.getPid()
				+ cfgNode.getId() + ".");
		graph.mergeSeq(control);

		CFG branch = traverse(control, root.getStatements(0));
		graph.mergeABranch(branch, control, "T");
		graph.addBackEdges(branch, control, "B");

		graph.adjustBreakNodes("");
		return graph;
	}

	private CFG traverse_do(CFGNode cfgNode, Statement root) {
		CFG graph = new CFG();
		if (root.getExpression() != null)
			graph.mergeSeq(traverse(cfgNode, root.getExpression()));

		CFGNode control = new CFGNode("DO", CFGNode.TYPE_CONTROL, "DO", "DO");
		control.setAstNode(root.getExpression());
		control.setPid((cfgNode == null) ? "." : cfgNode.getPid()
				+ cfgNode.getId() + ".");
		graph.mergeSeq(control);

		CFG branch = traverse(control, root.getStatements(0));
		graph.mergeABranch(branch, control, "T");
		graph.addBackEdges(branch, control, "B");

		graph.adjustBreakNodes("");
		return graph;
	}

	private CFG traverse_labeled(CFGNode cfgNode, Statement root) {
		CFG graph = traverse(cfgNode, root.getStatements(0));
		graph.adjustBreakNodes(root.getKind().name());
		return graph;
	}

	private CFG traverse_break(CFGNode cfgNode, Statement root) {
		CFG graph = new CFG();
		String label;
		if (root.getKind().name() == null) {
			label = "";
		} else {
			label = root.getKind().name();
		}
		CFGNode node = new CFGNode(label, CFGNode.TYPE_OTHER, "<GOTO>", label);
		node.setAstNode(root);
		graph.addBreakNode(node);
		return graph;
	}

	private CFG traverse_continue(CFGNode cfgNode, Statement root) {
		CFG graph = new CFG();
		String label;
		if (root.getKind().name() == null) {
			label = "";
		} else {
			label = root.getKind().name();
		}
		CFGNode node = new CFGNode(label, CFGNode.TYPE_OTHER, "<GOTO>", label);
		node.setAstNode(root);
		graph.addBreakNode(node);
		return graph;
	}

	private CFG traverse_throw(CFGNode cfgNode, Statement root) {
		CFG graph = new CFG();
		if (root.getExpression() != null)
			graph.mergeSeq(traverse(cfgNode, root.getExpression()));
		CFGNode node = new CFGNode("END[throw]", CFGNode.TYPE_OTHER, "<GOTO>",
				"END");
		node.setAstNode(root);
		graph.addReturnNode(node);
		return graph;
	}

	private CFG traverse_return(CFGNode cfgNode, Statement root) {
		CFG graph = new CFG();
		if (root.getExpression() != null)
			graph.mergeSeq(traverse(cfgNode, root.getExpression()));
		CFGNode node = new CFGNode("END[return]", CFGNode.TYPE_OTHER, "<GOTO>",
				"END");
		node.setAstNode(root);
		graph.addReturnNode(node);
		return graph;
	}

	private CFG traverse_try(CFGNode cfgNode, Statement root) {
		CFG graph = new CFG();
		CFGNode branch = new CFGNode("TRY", CFGNode.TYPE_CONTROL, "TRY", "TRY");
		branch.setAstNode(root);
		branch.setPid((cfgNode == null) ? "." : cfgNode.getPid()
				+ cfgNode.getId() + ".");
		graph.mergeSeq(branch);
		graph.mergeABranch(traverse(branch, root.getStatements(0)), branch, "T");
		// All catch statements are considered false branches.
		for (int i = 1; i < root.getStatementsCount(); i++) {
			graph.mergeABranch(traverse(branch, root.getStatements(i)), branch,
					"F");
		}
		return graph;
	}

	private CFG traverse_catch(CFGNode cfgNode, Statement root) {
		CFG graph = new CFG();
		CFGNode aNode = new CFGNode("CATCH", CFGNode.TYPE_OTHER, "CATCH",
				"CATCH");
		aNode.setAstNode(root);
		graph.mergeSeq(aNode);
		if (root.getStatementsCount() > 0)
			graph.mergeSeq(traverse(cfgNode, root.getStatements(0)));
		return graph;
	}

	private CFG traverse_sync(CFGNode cfgNode, Statement root) {
		CFG graph = new CFG();
		CFGNode aNode = new CFGNode();
		aNode.setAstNode(root.getExpression());
		aNode.setPid((cfgNode == null) ? "." : cfgNode.getPid()
				+ cfgNode.getId() + ".");
		graph.mergeSeq(traverse(cfgNode, root.getExpression()));
		graph.mergeSeq(aNode);
		for (int i = 0; i < root.getStatementsCount(); i++)
			graph.mergeSeq(traverse(aNode, root.getStatements(i)));
		return graph;
	}

	private CFGNode[] sortNodes() {
		try {
			CFGNode[] results = new CFGNode[nodes.size()];
			for (CFGNode node : nodes) {
				if (node.getId() >= nodes.size())
					System.out.println("NodeId Error");
				results[node.getId()] = node;
			}
			return results;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("List of nodes:" + nodes.size() + "\r\n");
		CFGNode[] mynodes = sortNodes();
		for (CFGNode node : mynodes) {
			sb.append("node " + node.getId() + " \t " + node.getMethod()
					+ "\r\n");
		}
		int numEdges = 0;
		sb.append("List of edges:" + "\r\n");
		for (CFGNode node : mynodes) {
			for (CFGEdge edge : node.getOutEdges()) {
				CFGNode anoNode = edge.getDest();
				if (!anoNode.getInEdges().contains(edge)) {
					System.err.println("ERRORERRORERRORERRORERRORERROR");
					System.err.println(node.getId() + "-" + anoNode.getId());
				}
				sb.append("node " + node.getId() + " --> node "
						+ anoNode.getId() + "\r\n");
				numEdges++;
			}
		}
		sb.append("Total " + numEdges + " edges" + "\r\n");
		return sb.toString();
	}

	public Builder newBuilder() {
		int size = nodes.size();
		Builder b = boa.types.Control.CFG.newBuilder();

		for (Iterator<CFGNode> nodesIter = nodes.iterator(); nodesIter
				.hasNext();) {
			b.addNodes(nodesIter.next().newBuilder());
		}

		CFGNode[] sortedNodes = sortNodes();
		Map<Integer, CFGEdgeLabel> edgeLabels = new HashMap<Integer, CFGEdgeLabel>();
		for (CFGNode node : sortedNodes) {
			for (CFGEdge edge : node.getOutEdges()) {
				CFGNode anoNode = edge.getDest();
				if (!anoNode.getInEdges().contains(edge)) {
					System.out.println("Error, wrong edge");
				}
				int index = node.getId() * size + anoNode.getId();
				edgeLabels.put(index, getLabel(edge.getLabel()));
			}
		}

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				int index = i * size + j;
				boa.types.Control.CFGEdge.Builder eb = boa.types.Control.CFGEdge.newBuilder();
				if (edgeLabels.containsKey(index))
					eb.setLabel(edgeLabels.get(index));
				else
					eb.setLabel(boa.types.Control.CFGEdge.CFGEdgeLabel.NIL);
				b.addEdges(eb.build());
			}
		}
		return b;
	}

	private final CFGEdgeLabel getLabel(String label) {
		if (label.equals(".")) {
			return CFGEdgeLabel.DEFAULT;
		} else if (label.equals("T")) {
			return CFGEdgeLabel.TRUE;
		} else if (label.equals("F")) {
			return CFGEdgeLabel.FALSE;
		} else if (label.equals("B")) {
			return CFGEdgeLabel.BACKEDGE;
		} else if (label.equals("E")) {
			return CFGEdgeLabel.EXITEDGE;
		} else {
			return CFGEdgeLabel.NIL;
		}
	}
}
