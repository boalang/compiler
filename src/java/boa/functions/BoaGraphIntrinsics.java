/*
 * Copyright 2017, Hridesh Rajan, Ganesha Upadhyaya, Ramanathan Ramu
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
 */
public class BoaGraphIntrinsics {
	@FunctionSpec(name = "getcfg", returnType = "CFG", formalParameters = { "Method" })
	public static CFG getcfg(final Method method) {
		final CFG cfg = new CFG(method);
		cfg.astToCFG();
		return cfg;
	}

	@FunctionSpec(name = "get_nodes_with_definition", returnType = "set of string", formalParameters = { "CFGNode" })
	public static LinkedHashSet<String> getNodesWithDefinition(final CFGNode node) {
		final LinkedHashSet<String> vardef = new LinkedHashSet<String>();
		if (node.getExpression() != null) {
			if (node.getExpression().getKind() == ExpressionKind.VARDECL || node.getExpression().getKind() == ExpressionKind.ASSIGN) {
				vardef.add(String.valueOf(node.getId()));
			}
		}
		return vardef;
	}

	@FunctionSpec(name = "get_variable_killed", returnType = "set of string", formalParameters = {"CFG", "CFGNode" })
	public static LinkedHashSet<String> getVariableKilled(final boa.types.Control.CFG cfg, final CFGNode node) {
		final LinkedHashSet<String> varkilled = new LinkedHashSet<String>();
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
	public static LinkedHashSet<String> getVariableDef(final CFGNode node) {
		final LinkedHashSet<String> vardef = new LinkedHashSet<String>();
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
	public static LinkedHashSet<String> getVariableUsed(final CFGNode node) {
		final LinkedHashSet<String> varused = new LinkedHashSet<String>();
		if (node.getExpression() != null) {
			traverseExpr(varused,node.getExpression());
		}
		return varused;
	}

	public static void traverseExpr(final LinkedHashSet<String> varused, final Expression expr) {
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

	public static void traverseVarDecls(final LinkedHashSet<String> varused, final Variable vardecls) {
		if (vardecls.getInitializer() != null) {
			traverseExpr(varused, vardecls.getInitializer());
		}
	}
}
