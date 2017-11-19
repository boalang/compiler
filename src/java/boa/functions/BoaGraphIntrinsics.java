/*
 * Copyright 2017, Hridesh Rajan, Ganesha Upadhyaya, Ramanathan Ramu, Robert Dyer, Che Shian Hung
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
package boa.functions;

import java.util.*;

import boa.graphs.cfg.CFG;
import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Method;
import boa.types.Ast.Variable;
import boa.types.Control.CFGNode;

/**
 * Boa functions for working with control flow graphs.
 *
 * @author ganeshau
 * @author rramu
 * @author rdyer
 * @author hungc
 */
public class BoaGraphIntrinsics {
	@FunctionSpec(name = "getcfg", returnType = "CFG", formalParameters = { "Method" })
	public static CFG getcfg(final Method method) {
		final CFG cfg = new CFG(method);
		cfg.astToCFG();
		return cfg;
	}

	@FunctionSpec(name = "get_nodes_with_definition", returnType = "set of string", formalParameters = { "CFGNode" })
	public static HashSet<String> getNodesWithDefinition(final CFGNode node) {
		final HashSet<String> vardef = new HashSet<String>();
		if (node.getExpression() != null) {
			if (node.getExpression().getKind() == ExpressionKind.VARDECL || node.getExpression().getKind() == ExpressionKind.ASSIGN) {
				vardef.add(String.valueOf(node.getId()));
			}
		}
		return vardef;
	}

	@FunctionSpec(name = "get_variable_killed", returnType = "set of string", formalParameters = {"CFG", "CFGNode" })
	public static HashSet<String> getVariableKilled(final boa.types.Control.CFG cfg, final CFGNode node) {
		final HashSet<String> varkilled = new HashSet<String>();
		String vardef = "";

		if (node.getExpression() != null) {
			if (node.getExpression().getKind() == ExpressionKind.VARDECL) {
				vardef = node.getExpression().getVariableDeclsList().get(0).getName();
			}
			else if (node.getExpression().getKind() == ExpressionKind.ASSIGN) {
				vardef = node.getExpression().getExpressionsList().get(0).getVariable();
			}
			else {
				return varkilled;
			}

			for (final CFGNode tnode : cfg.getNodesList()) {
				if (tnode.getExpression() != null && tnode.getId() != node.getId()) {
					if (tnode.getExpression().getKind() == ExpressionKind.VARDECL) {
						if (tnode.getExpression().getVariableDeclsList().get(0).getName().equals(vardef)) {
							varkilled.add(String.valueOf(tnode.getId()));
						}
					}
					else if (tnode.getExpression().getKind() == ExpressionKind.ASSIGN) {
						if (tnode.getExpression().getExpressionsList().get(0).getVariable().equals(vardef)) {
							varkilled.add(String.valueOf(tnode.getId()));
						}
					}
				}
			}
		}

		return varkilled;
	}

	@FunctionSpec(name = "get_variable_def", returnType = "set of string", formalParameters = { "CFGNode" })
	public static HashSet<String> getVariableDef(final CFGNode node) {
		final HashSet<String> vardef = new HashSet<String>();
		if (node.getExpression() != null) {
			if (node.getExpression().getKind() == ExpressionKind.VARDECL) {
				vardef.add(node.getExpression().getVariableDeclsList().get(0).getName());
			}
			else if (node.getExpression().getKind() == ExpressionKind.ASSIGN) {
				vardef.add(node.getExpression().getExpressionsList().get(0).getVariable());
			}
		}
		return vardef;
	}

	@FunctionSpec(name = "get_variable_used", returnType = "set of string", formalParameters = { "CFGNode" })
	public static HashSet<String> getVariableUsed(final CFGNode node) {
		final HashSet<String> varused = new HashSet<String>();
		if (node.getExpression() != null) {
			traverseExpr(varused,node.getExpression());
		}
		return varused;
	}

	public static void traverseExpr(final HashSet<String> varused, final Expression expr) {
		if (expr.getVariable() != null) {
			varused.add(expr.getVariable());
		}
		for (final Expression exprs : expr.getExpressionsList()) {
			traverseExpr(varused, exprs);
		}
		for (final Variable vardecls : expr.getVariableDeclsList()) {
			traverseVarDecls(varused, vardecls);
		}
		for (final Expression methodexpr : expr.getMethodArgsList()) {
			traverseExpr(varused, methodexpr);
		}
	}

	public static void traverseVarDecls(final HashSet<String> varused, final Variable vardecls) {
		if (vardecls.getInitializer() != null) {
			traverseExpr(varused, vardecls.getInitializer());
		}
	}

	private static String dotEscape(final String s) {
		final String escaped = s.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\l").replaceAll("\r", "\\\\l");
		if (escaped.indexOf("\\l") != -1 && !escaped.endsWith("\\l"))
			return escaped + "\\l";
		return escaped;
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "CFG" })
	public static String cfgToDot(final CFG cfg) {
		return cfgToDot(cfg, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "CFG", "bool" })
	public static String cfgToDot(final CFG cfg, final boolean showMethod) {
		if (showMethod)
			return cfgToDot(cfg, boa.functions.BoaAstIntrinsics.prettyprint(cfg.getMd()));
		return cfgToDot(cfg, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "CFG", "string" })
	public static String cfgToDot(final CFG cfg, final String label) {
		final StringBuilder str = new StringBuilder();
		str.append("digraph {\n");
		if (label.length() > 0) {
			str.append("\tlabelloc=\"t\"\n");
			str.append("\tlabel=\"" + dotEscape(label) + "\"\n");
		}

		for (final boa.graphs.cfg.CFGNode n : cfg.getNodes()) {
			final String shape;
			switch (n.getKind()) {
				case CONTROL:
					shape = "shape=diamond";
					break;
				case METHOD:
					shape = "shape=parallelogram";
					break;
				case OTHER:
					shape = "shape=box";
					break;
				case ENTRY:
				default:
					shape = "shape=ellipse";
					break;
			}

			if (n.hasStmt())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getStmt())) + "\"]\n");
			else if (n.hasExpr())
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getExpr())) + "\"]\n");
			else if (n.getKind() == boa.types.Control.CFGNode.CFGNodeType.ENTRY)
				str.append("\t" + n.getId() + "[" + shape + ",label=\"" + n.getName() + "\"]\n");
			else
				str.append("\t" + n.getId() + "[" + shape + "]\n");
		}

		final boa.runtime.BoaAbstractTraversal printGraph = new boa.runtime.BoaAbstractTraversal<Object>(false, false) {
			protected Object preTraverse(final boa.graphs.cfg.CFGNode node) throws Exception {
				final java.util.Set<boa.graphs.cfg.CFGEdge> edges = node.getOutEdges();
				for (final boa.graphs.cfg.CFGEdge e : node.getOutEdges()) {
					str.append("\t" + node.getId() + " -> " + e.getDest().getId());
					if (!(e.label() == null || e.label().equals(".") || e.label().equals("")))
						str.append(" [label=\"" + dotEscape(e.label()) + "\"]");
					str.append("\n");
				}
				return null;
			}

			@Override
			public void traverse(final boa.graphs.cfg.CFGNode node, boolean flag) throws Exception {
				if (flag) {
					currentResult = preTraverse(node);
					outputMapObj.put(node.getId(), currentResult);
				} else {
					outputMapObj.put(node.getId(), preTraverse(node));
				}
			}
		};

		try {
			printGraph.traverse(cfg, boa.types.Graph.Traversal.TraversalDirection.FORWARD, boa.types.Graph.Traversal.TraversalKind.DFS);
		} catch (final Exception e) {
			// do nothing
		}

		str.append("}");

		return str.toString();
	}
}
