/*
 * Copyright 2018, Hridesh Rajan, Ganesha Upadhyaya, Robert Dyer,
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

import java.util.HashMap;
import java.util.HashSet;

import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Statement;
import boa.types.Control.CFGNode.Builder;
import boa.types.Control.CFGNode.CFGNodeType;

/**
 * Control flow graph builder node
 *
 * @author ganeshau
 * @author rdyer
 */
public class CFGNode implements Comparable<CFGNode> {
	public static int numOfNodes = -1;

	private int id;
	private int methodId;
	private int objectNameId;
	private int classNameId;
	private int numOfParameters = 0;
	private HashSet<Integer> parameters;
	private CFGNodeType kind = CFGNodeType.OTHER;
	private String pid;
	private Statement stmt;
	private Expression expr;

	private Expression rhs;

	public static HashMap<String, Integer> idOfLabel = new HashMap<String, Integer>();
	public static HashMap<Integer, String> labelOfID = new HashMap<Integer, String>();

	public HashSet<CFGEdge> inEdges = new HashSet<CFGEdge>();
	public HashSet<CFGEdge> outEdges = new HashSet<CFGEdge>();

	public java.util.ArrayList<CFGNode> predecessors = new java.util.ArrayList<CFGNode>();
	public java.util.ArrayList<CFGNode> successors = new java.util.ArrayList<CFGNode>();

	public HashSet<String> useVariables = new HashSet<String>();
	public String defVariables;

	@Override
	public int compareTo(final CFGNode node) {
		return node.id - this.id;
	}

	public CFGNode() {
		this.id = ++numOfNodes;
	}

	public CFGNode(final CFGNode tmp) {
	}

	public CFGNode(final String methodName, final CFGNodeType kind, final String className,
			final String objectName) {
		this.id = ++numOfNodes;
		this.methodId = convertLabel(methodName);
		this.kind = kind;
		this.classNameId = convertLabel(className);
		this.objectNameId = convertLabel(objectName);
	}

	public CFGNode(final String methodName, final CFGNodeType kind, final String className,
			final String objectName, final int numOfParameters, final HashSet<Integer> datas) {
		this.id = ++numOfNodes;
		this.methodId = convertLabel(methodName);
		this.kind = kind;

		if (className == null) {
			this.classNameId = -1;
		} else {
			this.classNameId = convertLabel(className);
		}

		this.objectNameId = convertLabel(objectName);
		this.parameters = new HashSet<Integer>(datas);
		this.numOfParameters = numOfParameters;
	}

	public CFGNode(final String methodName, final CFGNodeType kind, final String className,
			final String objectName, final int numOfParameters) {
		this.id = ++numOfNodes;
		this.methodId = convertLabel(methodName);
		this.kind = kind;
		this.classNameId = convertLabel(className);
		this.objectNameId = convertLabel(objectName);
		this.numOfParameters = numOfParameters;
	}

	public Statement getStmt() {
		return this.stmt;
	}

	public HashSet<String> getDefUse() {
		final HashSet<String> defUse = new HashSet<String>(this.useVariables);
		defUse.add(defVariables);
		return defUse;
	}

	public boolean hasStmt() {
		return this.stmt != null;
	}

	public boolean hasDefVariables() {
		return this.defVariables != null;
	}

	public Expression getExpr() {
		return this.expr;
	}

	public Expression getRhs() {
		return this.rhs;
	}

	public void setRhs(final Expression rhs) {
		this.rhs = rhs;
	}

	public boolean hasExpr() {
		return this.expr != null;
	}

	public boolean hasRhs() {
		return this.rhs != null;
	}

	public static int convertLabel(final String label) {
		if (!CFGNode.idOfLabel.containsKey(label)) {
			final int index = CFGNode.idOfLabel.size() + 1;
			CFGNode.idOfLabel.put(label, index);
			CFGNode.labelOfID.put(index, label);
			return index;
		}
		return CFGNode.idOfLabel.get(label);
	}

	public int getId() {
		return this.id;
	}

	public int getNumOfParameters() {
		return this.numOfParameters;
	}

	public void setParameters(final HashSet<Integer> parameters) {
		this.parameters = parameters;
	}

	public HashSet<Integer> getParameters() {
		return this.parameters;
	}

	public void setUseVariables(final HashSet<String> useVariables) {
		this.useVariables = useVariables;
	}

	public void setDefVariables(final String defVariables) {
		this.defVariables = defVariables;
	}

	public int getClassNameId() {
		return this.classNameId;
	}

	public int getObjectNameId() {
		return this.objectNameId;
	}

	public String getObjectName() {
		return labelOfID.get(this.objectNameId);
	}

	public String getClassName() {
		return labelOfID.get(this.classNameId);
	}

	public HashSet<String> getUseVariables() {
		return this.useVariables;
	}

	public String getDefVariables() {
		return this.defVariables;
	}

	public boolean hasFalseBranch() {
		for (final CFGEdge e : this.outEdges) {
			if (e.label().equals("F"))
				return true;
		}
		return false;
	}

	public HashSet<CFGEdge> getInEdges() {
		return this.inEdges;
	}

	public HashSet<CFGEdge> getOutEdges() {
		return this.outEdges;
	}

	public java.util.ArrayList<CFGNode> getPredecessorsList() {
		return this.predecessors;
	}

	public java.util.ArrayList<CFGNode> getSuccessorsList() {
		return this.successors;
	}

	public void setPredecessors(final java.util.ArrayList<CFGNode> predecessors) {
		this.predecessors = predecessors;
	}

	public void setSuccessors(final java.util.ArrayList<CFGNode> successors) {
		this.successors = successors;
	}

	public java.util.ArrayList<CFGNode> getInNodes() {
		final HashSet<CFGNode> nodes = new HashSet<CFGNode>();
		for (final CFGEdge e : this.inEdges)
			nodes.add(e.getSrc());
		return new java.util.ArrayList<CFGNode>(nodes);
	}

	public java.util.ArrayList<CFGNode> getOutNodes() {
		final HashSet<CFGNode> nodes = new HashSet<CFGNode>();
		for (final CFGEdge e : this.outEdges)
			nodes.add(e.getDest());
		return new java.util.ArrayList<CFGNode>(nodes);
	}

	public CFGEdge getOutEdge(final CFGNode node) {
		for (final CFGEdge e : this.outEdges) {
			if (e.getDest() == node)
				return e;
		}
		return null;
	}

	public CFGEdge getInEdge(final CFGNode node) {
		for (final CFGEdge e : this.inEdges) {
			if (e.getSrc() == node)
				return e;
		}
		return null;
	}

	public String getPid() {
		return this.pid;
	}

	public String getMethod() {
		return CFGNode.labelOfID.get(this.methodId);
	}

	public void setPid(final String pid) {
		this.pid = pid;
	}

	public void addInEdge(final CFGEdge edge) {
		this.inEdges.add(edge);
	}

	public void addOutEdge(final CFGEdge edge) {
		this.outEdges.add(edge);
	}

	public void setAstNode(final Statement stmt) {
		this.stmt = stmt;
	}

	public void setAstNode(final Expression expr) {
		this.expr = expr;
	}

	public String getName() {
		String name = getMethod();
		if (name == null)
			name = getObjectName();
		if (name == null)
			name = getClassName();
		if (name == null)
			name = "";
		return name;
	}

	public Builder newBuilder() {
		final Builder b = boa.types.Control.CFGNode.newBuilder();
		b.setId(this.id);
		b.setKind(this.kind);
		if (this.stmt != null)
			b.setStatement(this.stmt);
		else if (this.expr != null)
			b.setExpression(this.expr);
		return b;
	}

	public CFGNodeType getKind() {
		return this.kind;
	}

	public String processDef() {
		String defVar = "";
		if (this.expr != null) {
			if (this.expr.getKind() == ExpressionKind.VARDECL) {
				final String[] strComponents = this.expr.getVariableDeclsList().get(0).getName().split("\\.");
				if (strComponents.length > 1) {
					defVar = strComponents[strComponents.length - 2];
				} else {
					defVar = strComponents[0];
				}
			} else if (this.expr.getKind() == ExpressionKind.OP_INC || this.expr.getKind() == ExpressionKind.OP_DEC) {
				if (this.expr.getExpressionsList().get(0).hasVariable()) {
					final String[] strComponents = this.expr.getExpressionsList().get(0).getVariable().split("\\.");
					if (strComponents.length > 1) {
						defVar = strComponents[strComponents.length - 2];
					} else {
						defVar = strComponents[0];
					}
				}
			} else if (this.expr.getKind().toString().startsWith("ASSIGN")) {
				final String[] strComponents = this.expr.getExpressionsList().get(0).getVariable().split("\\.");
				if (strComponents.length > 1) {
					defVar = strComponents[strComponents.length - 2];
				} else {
					defVar = strComponents[0];
				}
			}
		}
		return defVar;
	}

	public HashSet<String> processUse() {
		final HashSet<String> useVar = new HashSet<String>();
		if (this.expr != null) {
			if (this.expr.getKind() == ExpressionKind.ASSIGN) {
				processUse(useVar, this.rhs);
			} else {
				processUse(useVar, this.expr);
			}
		}
		return useVar;
	}

	protected static void processUse(final HashSet<String> useVar, final boa.types.Ast.Expression expr) {
		if (expr.hasVariable()) {
			if (expr.getExpressionsList().size() != 0) {
				useVar.add("this");
			} else {
				final String[] strComponents = expr.getVariable().split("\\.");
				if (strComponents.length > 1) {
					useVar.add(strComponents[strComponents.length - 2]);
				} else {
					useVar.add(strComponents[0]);
				}
			}
		}
		for (final boa.types.Ast.Expression exprs : expr.getExpressionsList()) {
			processUse(useVar, exprs);
		}
		for (final boa.types.Ast.Variable vardecls : expr.getVariableDeclsList()) {
			processUse(useVar, vardecls);
		}
		for (final boa.types.Ast.Expression methodexpr : expr.getMethodArgsList()) {
			processUse(useVar, methodexpr);
		}
	}

	protected static void processUse(final HashSet<String> useVar, final boa.types.Ast.Variable vardecls) {
		if (vardecls.hasInitializer()) {
			processUse(useVar, vardecls.getInitializer());
		}
	}

	public String toString() {
		return "node " + getId();
	}
}
