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

import boa.graphs.cdg.CDG;
import boa.graphs.cfg.CFG;
import boa.graphs.ddg.DDG;
import boa.graphs.pdg.PDG;
import boa.graphs.slicers.CFGSlicer;
import boa.graphs.slicers.PDGSlicer;
import boa.graphs.trees.PDTree;
import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Method;
import boa.types.Ast.Variable;
import boa.types.Control.Node;

/**
 * Boa functions for working with control flow graphs.
 *
 * @author ganeshau
 * @author rramu
 * @author rdyer
 * @author hungc
 * @author marafat
 */
public class BoaGraphIntrinsics {
	@FunctionSpec(name = "getcfg", returnType = "CFG", formalParameters = { "Method" })
	public static CFG getcfg(final Method method) {
		return new CFG(method).get();
	}

	@FunctionSpec(name = "getpdtree", returnType = "PDTree", formalParameters = { "Method" })
	public static PDTree getpdtree(final Method method) throws Exception {
		return new PDTree(method);
	}

	@FunctionSpec(name = "getcdg", returnType = "CDG", formalParameters = { "Method" })
	public static CDG getcdg(final Method method) throws Exception {
		return new CDG(method);
	}

	@FunctionSpec(name = "getcdg", returnType = "CDG", formalParameters = { "CFG" })
	public static CDG getcdg(final CFG cfg) throws Exception {
		return new CDG(cfg);
	}

	@FunctionSpec(name = "getddg", returnType = "DDG", formalParameters = { "Method" })
	public static DDG getddg(final Method method) throws Exception {
		return new DDG(method);
	}

	@FunctionSpec(name = "getddg", returnType = "DDG", formalParameters = { "CFG" })
	public static DDG getddg(final CFG cfg) throws Exception {
		return new DDG(cfg);
	}

	@FunctionSpec(name = "getpdg", returnType = "PDG", formalParameters = { "Method" })
	public static PDG getpdg(final Method method) throws Exception {
		return new PDG(method);
	}

	@FunctionSpec(name = "getpdg", returnType = "PDG", formalParameters = { "Method", "bool" })
	public static PDG getpdg(final Method method, boolean paramAsStatement) throws Exception {
		return new PDG(method, paramAsStatement);
	}

	@FunctionSpec(name = "getcfgslice", returnType = "CFGSlicer", formalParameters = { "Method", "int" })
	public static CFGSlicer getcfgslice(final Method method, Long id) throws Exception {
		return new CFGSlicer(method, (int)(long) id);
	}

	@FunctionSpec(name = "getpdgslice", returnType = "PDGSlicer", formalParameters = { "PDG",  "int", "bool" })
	public static PDGSlicer getpdgslice(final PDG pdg, Long id, boolean normalize) throws Exception {
		return new PDGSlicer(pdg, (int)(long) id, normalize);
	}

	@FunctionSpec(name = "getpdgslice", returnType = "PDGSlicer", formalParameters = { "Method",  "int", "bool" })
	public static PDGSlicer getpdgslice(final Method method, Long id, boolean normalize) throws Exception {
		return new PDGSlicer(method, (int)(long) id, normalize);
	}

	@FunctionSpec(name = "get_nodes_with_definition", returnType = "set of string", formalParameters = { "Node" })
	public static HashSet<String> getNodesWithDefinition(final Node node) {
		final HashSet<String> vardef = new HashSet<String>();
		if (node.getExpression() != null) {
			if (node.getExpression().getKind() == ExpressionKind.VARDECL || node.getExpression().getKind() == ExpressionKind.ASSIGN) {
				vardef.add(String.valueOf(node.getId()));
			}
		}
		return vardef;
	}

	@FunctionSpec(name = "get_variable_killed", returnType = "set of string", formalParameters = {"CFG", "Node" })
	public static HashSet<String> getVariableKilled(final boa.types.Control.Graph cfg, final Node node) {
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

			for (final Node tnode : cfg.getNodesList()) {
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

	@FunctionSpec(name = "get_variable_def", returnType = "set of string", formalParameters = { "Node" })
	public static HashSet<String> getVariableDef(final Node node) {
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

	@FunctionSpec(name = "get_variable_used", returnType = "set of string", formalParameters = { "Node" })
	public static HashSet<String> getVariableUsed(final Node node) {
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
		final String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\l").replace("\n", "\\l");
		if (escaped.indexOf("\\l") != -1 && !escaped.endsWith("\\l"))
			return escaped + "\\l";
		return escaped;
	}

	// Graph Visualizers

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
		if (cfg == null || cfg.getNodes().size() == 0) return "";
		final StringBuilder str = new StringBuilder();
		final StringBuilder str2 = new StringBuilder();
		str.append("digraph {\n");
		str.append("\t{ rank = source; 0; }\n");
		str.append("\t{ rank = sink; ");
		str.append(String.valueOf(cfg.getNodes().size() - 1));
		str.append("; }\n");
		if (label.length() > 0) {
			str.append("\tlabelloc=\"t\"\n");
			str.append("\tlabel=\"");
			str.append(dotEscape(label));
			str.append("\"\n");
		}

		final boa.graphs.cfg.CFGNode[] sorted = cfg.sortNodes();
		for (final boa.graphs.cfg.CFGNode n : sorted) {
			str.append("\t");
			str.append(n.getId());
			str.append("[");
			switch (n.getKind()) {
				case CONTROL:
					str.append("shape=diamond");
					break;
				case METHOD:
					str.append("shape=parallelogram");
					break;
				case OTHER:
					str.append("shape=box");
					break;
				case ENTRY:
				default:
					str.append("shape=ellipse");
					break;
			}
			if (n.hasStmt()) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ");
				str.append(dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getStmt())));
				str.append("\"]\n");
			} else if (n.hasExpr()) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ");
				str.append(dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getExpr())));
				str.append("\"]\n");
			} else if (n.getKind() == boa.types.Control.Node.NodeType.ENTRY) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ");
				str.append(n.getName());
				str.append("\"]\n");
			} else {
				str.append("]\n");
			}

			final java.util.List<boa.graphs.cfg.CFGEdge> edges = new ArrayList<boa.graphs.cfg.CFGEdge>(n.getOutEdges());
			Collections.sort(edges);
			for (final boa.graphs.Edge e : edges) {
				str2.append("\t");
				str2.append(n.getId());
				str2.append(" -> ");
				str2.append(e.getDest().getId());
				if (e.getLabel() != null && !e.getLabel().isEmpty() && !e.getLabel().equals(".")) {
					str2.append(" [label=\"");
					str2.append(dotEscape(e.getLabel()));
					str2.append("\"]");
				}
				str2.append("\n");
			}
		}

		str.append(str2);
		str.append("}");

		return str.toString();
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "CDG" })
	public static String cdgToDot(final CDG cdg) {
		return cdgToDot(cdg, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "CDG", "bool" })
	public static String cdgToDot(final CDG cdg, final boolean showMethod) {
		if (showMethod)
			return cdgToDot(cdg, boa.functions.BoaAstIntrinsics.prettyprint(cdg.getMethod()));
		return cdgToDot(cdg, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "CDG", "string" })
	public static String cdgToDot(final CDG cdg, final String label) {
		if (cdg == null || cdg.getNodes().size() == 0) return "";
		final StringBuilder str = new StringBuilder();
		final StringBuilder str2 = new StringBuilder();
		str.append("digraph {\n");
		str.append("\t{ rank = source; 0; }\n");
		if (label.length() > 0) {
			str.append("\tlabelloc=\"t\"\n");
			str.append("\tlabel=\"");
			str.append(dotEscape(label));
			str.append("\"\n");
		}

		for (final boa.graphs.cdg.CDGNode n : cdg.getNodes()) {
			str.append("\t");
			str.append(n.getId());
			str.append("[shape=ellipse");
			if (n.hasStmt()) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ");
				str.append(dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getStmt())));
				str.append("\"]\n");
			} else if (n.hasExpr()) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ");
				str.append(dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getExpr())));
				str.append("\"]\n");
			} else if (n.getKind() == boa.types.Control.Node.NodeType.ENTRY) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ENTRY\"]\n");
			} else {
				str.append("]\n");
			}

			final java.util.List<boa.graphs.cdg.CDGEdge> edges = new ArrayList<boa.graphs.cdg.CDGEdge>(n.getOutEdges());
			Collections.sort(edges);
			for (final boa.graphs.cdg.CDGEdge e : edges) {
				str2.append("\t");
				str2.append(n.getId());
				str2.append(" -> ");
				str2.append(e.getDest().getId());
				if (!(e.getLabel() == null || e.getLabel().equals(".") || e.getLabel().equals(""))) {
					str2.append(" [label=\"");
					str2.append(dotEscape(e.getLabel()));
					str2.append("\"]");
				}
				str2.append("\n");
			}
		}

		str.append(str2);
		str.append("}");

		return str.toString();
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "DDG" })
	public static String ddgToDot(final DDG ddg) {
		return ddgToDot(ddg, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "DDG", "bool" })
	public static String ddgToDot(final DDG ddg, final boolean showMethod) {
		if (showMethod)
			return ddgToDot(ddg, boa.functions.BoaAstIntrinsics.prettyprint(ddg.getMethod()));
		return ddgToDot(ddg, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "DDG", "string" })
	public static String ddgToDot(final DDG ddg, final String label) {
		if (ddg == null || ddg.getNodes().size() == 0) return "";
		final StringBuilder str = new StringBuilder();
		final StringBuilder str2 = new StringBuilder();
		str.append("digraph {\n");
		str.append("\t{ rank = source; 0; }\n");
		if (label.length() > 0) {
			str.append("\tlabelloc=\"t\"\n");
			str.append("\tlabel=\"");
			str.append(dotEscape(label));
			str.append("\"\n");
		}

		final boa.graphs.ddg.DDGNode[] sorted = ddg.sortNodes();
		for (final boa.graphs.ddg.DDGNode n : sorted) {
			str.append("\t");
			str.append(n.getId());
			str.append("[shape=ellipse");
			if (n.hasStmt()) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ");
				str.append(dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getStmt())));
				str.append("\"]\n");
			} else if (n.hasExpr()) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ");
				str.append(dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getExpr())));
				str.append("\"]\n");
			} else if (n.getKind() == boa.types.Control.Node.NodeType.ENTRY) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ENTRY\"]\n");
			} else {
				str.append("]\n");
			}

			final java.util.List<boa.graphs.ddg.DDGEdge> edges = new ArrayList<boa.graphs.ddg.DDGEdge>(n.getOutEdges());
			Collections.sort(edges);
			for (final boa.graphs.ddg.DDGEdge e : edges) {
				str2.append("\t");
				str2.append(n.getId());
				str2.append(" -> ");
				str2.append(e.getDest().getId());
				if (e.getLabel() != null && !e.getLabel().isEmpty() && !e.getLabel().equals(".")) {
					str2.append(" [label=\"");
					str2.append(dotEscape(e.getLabel()));
					str2.append("\"]");
				}
				str2.append("\n");
			}
		}

		str.append(str2);
		str.append("}");

		return str.toString();
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDG" })
	public static String pdgToDot(final PDG pdg) {
		return pdgToDot(pdg, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDG", "bool" })
	public static String pdgToDot(final PDG pdg, final boolean showMethod) {
		if (showMethod)
			return pdgToDot(pdg, boa.functions.BoaAstIntrinsics.prettyprint(pdg.getMethod()));
		return pdgToDot(pdg, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDG", "string" })
	public static String pdgToDot(final PDG pdg, final String label) {
		if (pdg == null || pdg.getNodes().size() == 0) return "";
		final StringBuilder str = new StringBuilder();
		final StringBuilder str2 = new StringBuilder();
		str.append("digraph {\n");
		if (label.length() > 0) {
			str.append("\tlabelloc=\"t\"\n");
			str.append("\tlabel=\"");
			str.append(dotEscape(label));
			str.append("\"\n");
		}

		final boa.graphs.pdg.PDGNode[] sorted = pdg.sortNodes();
		for (final boa.graphs.pdg.PDGNode n : sorted) {
			str.append("\t");
			str.append(n.getId());
			str.append("[shape=ellipse");
			if (n.hasStmt()) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ");
				str.append(dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getStmt())));
				str.append("\"]\n");
			} else if (n.hasExpr()) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ");
				str.append(dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getExpr())));
				str.append("\"]\n");
			} else if (n.getKind() == boa.types.Control.Node.NodeType.ENTRY) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ENTRY\"]\n");
			} else {
				str.append("]\n");
			}

			final java.util.List<boa.graphs.pdg.PDGEdge> edges = new ArrayList<boa.graphs.pdg.PDGEdge>(n.getOutEdges());
			Collections.sort(edges);
			for (final boa.graphs.pdg.PDGEdge e : edges) {
				str2.append("\t");
				str2.append(n.getId());
				str2.append(" -> ");
				str2.append(e.getDest().getId());
				if (!(e.getLabel() == null || e.getLabel().equals(".") || e.getLabel().equals(""))) {
					str2.append(" [label=\"");
					str2.append(e.getKind());
					str2.append(":");
					str2.append(dotEscape(e.getLabel()));
					str2.append("\"]");
				}
				str2.append("\n");
			}
		}

		str.append(str2);
		str.append("}");

		return str.toString();
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDGSlicer" })
	public static String pdgslicerToDot(final PDGSlicer pdgslicer) {
		return pdgslicerToDot(pdgslicer, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDGSlicer", "bool" })
	public static String pdgslicerToDot(final PDGSlicer pdgslicer, final boolean showMethod) {
		if (showMethod)
			return pdgslicerToDot(pdgslicer, boa.functions.BoaAstIntrinsics.prettyprint(pdgslicer.getMethod()));
		return pdgslicerToDot(pdgslicer, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDGSlicer", "string" })
	public static String pdgslicerToDot(final PDGSlicer pdgslicer, final String label) {
		if (pdgslicer == null || pdgslicer.getSlice().size() == 0) return "";
		final StringBuilder str = new StringBuilder();
		final StringBuilder str2 = new StringBuilder();
		str.append("digraph {\n");
		if (label.length() > 0) {
			str.append("\tlabelloc=\"t\"\n");
			str.append("\tlabel=\"");
			str.append(dotEscape(label));
			str.append("\"\n");
		}

		for (final boa.graphs.pdg.PDGNode n : pdgslicer.getSlice()) {
			str.append("\t");
			str.append(n.getId());
			str.append("[shape=ellipse");
			if (n.hasStmt()) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ");
				str.append(dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getStmt())));
				str.append("\"]\n");
			} else if (n.hasExpr()) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ");
				str.append(dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getExpr())));
				str.append("\"]\n");
			} else if (n.getKind() == boa.types.Control.Node.NodeType.ENTRY) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ENTRY\"]\n");
			} else {
				str.append("]\n");
			}

			final java.util.List<boa.graphs.pdg.PDGEdge> edges = new ArrayList<boa.graphs.pdg.PDGEdge>(n.getOutEdges());
			Collections.sort(edges);
			for (final boa.graphs.pdg.PDGEdge e : edges) {
				str2.append("\t");
				str2.append(n.getId());
				str2.append(" -> ");
				str2.append(e.getDest().getId());
				if (!(e.getLabel() == null || e.getLabel().equals(".") || e.getLabel().equals(""))) {
					str2.append(" [label=\"");
					str2.append(e.getKind());
					str2.append(":");
					str2.append(dotEscape(e.getLabel()));
					str2.append("\"]");
				}
				str2.append("\n");
			}
		}

		str.append(str2);
		str.append("}");

		return str.toString();
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDTree" })
	public static String pdtreeToDot(final PDTree pdtree) {
		return pdtreeToDot(pdtree, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDTree", "bool" })
	public static String pdtreeToDot(final PDTree pdtree, final boolean showMethod) {
		if (showMethod)
			return pdtreeToDot(pdtree, boa.functions.BoaAstIntrinsics.prettyprint(pdtree.getMethod()));
		return pdtreeToDot(pdtree, "");
	}

	@FunctionSpec(name = "dot", returnType = "string", formalParameters = { "PDTree", "string" })
	public static String pdtreeToDot(final PDTree pdtree, final String label) {
		if (pdtree == null || pdtree.getNodes().size() == 0) return "";
		final StringBuilder str = new StringBuilder();
		final StringBuilder str2 = new StringBuilder();
		str.append("digraph {\n");
		if (label.length() > 0) {
			str.append("\tlabelloc=\"t\"\n");
			str.append("\tlabel=\"");
			str.append(dotEscape(label));
			str.append("\"\n");
		}

		for (final boa.graphs.trees.TreeNode n : pdtree.getNodes()) {
			str.append("\t");
			str.append(n.getId());
			str.append("[shape=ellipse");
			if (n.hasStmt()) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ");
				str.append(dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getStmt())));
				str.append("\"]\n");
			} else if (n.hasExpr()) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] ");
				str.append(dotEscape(boa.functions.BoaAstIntrinsics.prettyprint(n.getExpr())));
				str.append("\"]\n");
			} else if (n.getId() == 0) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] START\"]\n");
			} else if (n.getId() == pdtree.getNodes().size() - 1) {
				str.append(",label=\"[");
				str.append(n.getId());
				str.append("] STOP\"]\n");
			} else {
				str.append("]\n");
			}

			final java.util.List<boa.graphs.trees.TreeNode> nodes = new ArrayList<boa.graphs.trees.TreeNode>(n.getChildren());
			Collections.sort(nodes);
			for (final boa.graphs.trees.TreeNode node : nodes) {
				str2.append("\t");
				str2.append(n.getId());
				str2.append(" -> ");
				str2.append(node.getId());
				str2.append("\n");
			}
		}

		str.append(str2);
		str.append("}");

		return str.toString();
	}
}
