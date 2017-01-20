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

import boa.types.Ast.Expression;
import boa.types.Ast.Statement;
import boa.types.Control.CFGNode.Builder;
import boa.types.Control.CFGNode.CFGNodeType;
import java.util.Collections;

/**
 * Control flow graph builder node
 * @author ganeshau
 *
 */
public class CFGNode implements Comparable {
	public static final int TYPE_METHOD = 1;
	public static final int TYPE_CONTROL = 2;
	public static final int TYPE_ENTRY = 3;
	public static final int TYPE_OTHER = 4;
	public static int numOfNodes = -1;

	private int id;
	private String label;
	private int methodId;
	private int objectNameId;
	private int classNameId;
	private int numOfParameters = 0;
	private HashSet<Integer> parameters;
	private int kind = TYPE_OTHER;
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
	public int compareTo(Object node) {
		int nodeId=((CFGNode)node).id;
		return nodeId - id;
	}

	public CFGNode() {
		// TODO Auto-generated constructor stub
		this.id = ++numOfNodes;
	}

	public CFGNode(CFGNode tmp) {
	}

	public CFGNode(String label) {
		this.id = ++numOfNodes;
		this.label = label;
	}

	public CFGNode(String methodName, int kind, String className,
			String objectName) {
		this.id = ++numOfNodes;
		this.methodId = convertLabel(methodName);
		this.kind = kind;
		this.classNameId = convertLabel(className);
		this.objectNameId = convertLabel(objectName);
	}

	public CFGNode(String methodName, int kind, String className,
			String objectName, int numOfParameters, HashSet<Integer> datas) {
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

	public CFGNode(String methodName, int kind, String className,
			String objectName, int numOfParameters) {
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
		HashSet<String> defUse = new HashSet<String>(useVariables);
		defUse.add(defVariables);
		return defUse;
	}

	public boolean hasStmt() {
		if(this.stmt!=null) {
			return true;
		}
		return false;
	}

	public boolean hasDefVariables() {
		if(this.defVariables!=null) {
			return true;
		}
		return false;
	}

	public Expression getExpr() {
		return this.expr;
	}

	public Expression getRhs() {
		return this.rhs;
	}

	public void setRhs(Expression rhs) {
		this.rhs = rhs;
	}

	public boolean hasExpr() {
		if(this.expr!=null) {
			return true;
		}
		return false;
	}

	public boolean hasRhs() {
		if(this.rhs!=null) {
			return true;
		}
		return false;
	}

	public static int convertLabel(String label) {
		if (CFGNode.idOfLabel.get(label) == null) {
			int index = CFGNode.idOfLabel.size() + 1;
			CFGNode.idOfLabel.put(label, index);
			CFGNode.labelOfID.put(index, label);
			return index;
		} else
			return CFGNode.idOfLabel.get(label);
	}

	public int getId() {
		return id;
	}

	public int getNodeKind() {
		return kind;
	}

	public int getNumOfParameters() {
		return numOfParameters;
	}

	public void setParameters(HashSet<Integer> parameters) {
		this.parameters = parameters;
	}

	public HashSet<Integer> getParameters() {
		return parameters;
	}

	public void setUseVariables(HashSet<String> useVariables) {
		this.useVariables = useVariables;
	}

	public void setDefVariables(String defVariables) {
		this.defVariables = defVariables;
	}

	public int getClassNameId() {
		return classNameId;
	}

	public int getObjectNameId() {
		return objectNameId;
	}

	public String getObjectName() {
		return labelOfID.get(objectNameId);
	}

	public String getClassName() {
		return labelOfID.get(classNameId);
	}

	public HashSet<String> getUseVariables() {
		return useVariables;
	}

	public String getDefVariables() {
		return defVariables;
	}

	public boolean hasFalseBranch() {
		for (CFGEdge e : this.outEdges) {
			if (e.label().equals("F"))
				return true;
		}
		return false;
	}

	public HashSet<CFGEdge> getInEdges() {
		return inEdges;
	}

	public HashSet<CFGEdge> getOutEdges() {
		return outEdges;
	}

	public java.util.ArrayList<CFGNode> getPredecessorsList() {
		return predecessors;
	}

	public java.util.ArrayList<CFGNode> getSuccessorsList() {
		return successors;
	}

	public void setPredecessors(java.util.ArrayList<CFGNode> predecessors) {
		this.predecessors = predecessors;
	}

	public void setSuccessors(java.util.ArrayList<CFGNode> successors) {
		this.successors = successors;
	}

	public java.util.ArrayList<CFGNode> getInNodes() {
		HashSet<CFGNode> nodes = new HashSet<CFGNode>();
		for (CFGEdge e : inEdges)
			nodes.add(e.getSrc());
		java.util.ArrayList<CFGNode> pred = new java.util.ArrayList<CFGNode>(nodes);
		//Collections.sort(pred);
		return pred;
	}

	public java.util.ArrayList<CFGNode> getOutNodes() {
		HashSet<CFGNode> nodes = new HashSet<CFGNode>();
		for (CFGEdge e : outEdges)
			nodes.add(e.getDest());
		java.util.ArrayList<CFGNode> succ = new java.util.ArrayList<CFGNode>(nodes);
		//Collections.sort(succ);
		return succ;
	}

	public CFGEdge getOutEdge(CFGNode node) {
		for (CFGEdge e : this.outEdges) {
			if (e.getDest() == node)
				return e;
		}
		return null;
	}

	public CFGEdge getInEdge(CFGNode node) {
		for (CFGEdge e : this.inEdges) {
			if (e.getSrc() == node)
				return e;
		}
		return null;
	}

	public String getPid() {
		return pid;
	}

	public String getMethod() {
		return CFGNode.labelOfID.get(methodId);
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public void addInEdge(CFGEdge edge) {
		inEdges.add(edge);
	}

	public void addOutEdge(CFGEdge edge) {
		outEdges.add(edge);
	}

	public void setAstNode(Statement stmt) {
		this.stmt = stmt;
	}

	public void setAstNode(Expression expr) {
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
		Builder b = boa.types.Control.CFGNode.newBuilder();
		b.setId(id);
		b.setKind(getKind());
		if (this.stmt != null)
			b.setStatement(stmt);
		else if (this.expr != null)
			b.setExpression(expr);
		return b;
	}

	public CFGNodeType getKind() {
		switch (this.kind) {
		case TYPE_METHOD:
			return CFGNodeType.METHOD;
		case TYPE_CONTROL:
			return CFGNodeType.CONTROL;
		case TYPE_ENTRY:
			return CFGNodeType.ENTRY;
		case TYPE_OTHER:
		default:
			return CFGNodeType.OTHER;
		}
	}

	public String processDef() {
		String defVar="";
		if(this.expr!=null) {
			if(this.expr.getKind().toString().equals("VARDECL")) {
				String[] strComponents = this.expr.getVariableDeclsList().get(0).getName().split("\\.");
				if(strComponents.length > 1) {
					defVar = strComponents[strComponents.length - 2];
				}
				else {
					defVar = strComponents[0];
				}
			}
			if(this.expr.getKind().toString().equals("ASSIGN")) {
				String[] strComponents = this.expr.getExpressionsList().get(0).getVariable().split("\\.");
				if(strComponents.length > 1) {
					defVar = strComponents[strComponents.length - 2];
				}
				else {
					defVar = strComponents[0];
				}
			}
		}
		return defVar;
	}

	public HashSet<String> processUse() {
		HashSet<String> useVar= new HashSet<String>();
		if(this.expr!=null) {
			if(this.expr.getKind().toString().equals("ASSIGN")) {
				traverseExpr(useVar, this.rhs);
			}
			else {
				traverseExpr(useVar, this.expr);
			}
		}
		return useVar;
	}

	public static void traverseExpr(HashSet<String> useVar, final boa.types.Ast.Expression expr) {		
		if(expr.hasVariable()) {
			if(expr.getExpressionsList().size()!=0) {
				useVar.add("this");
			}
			else {
				String[] strComponents = expr.getVariable().split("\\.");
				if(strComponents.length > 1) {
					useVar.add(strComponents[strComponents.length - 2]);
				}
				else {
					useVar.add(strComponents[0]);
				}	
			}		
		}
		for(boa.types.Ast.Expression exprs:expr.getExpressionsList()) {
			traverseExpr(useVar, exprs);
		}
		for(boa.types.Ast.Variable vardecls:expr.getVariableDeclsList()) {
			traverseVarDecls(useVar, vardecls);
		}
		for(boa.types.Ast.Expression methodexpr:expr.getMethodArgsList()) {
			traverseExpr(useVar, methodexpr);
		}
	}

	public static void traverseVarDecls(HashSet<String> useVar, final boa.types.Ast.Variable vardecls) {		
		if(vardecls.hasInitializer()) {
			traverseExpr(useVar, vardecls.getInitializer());			
		}
	}

	public String toString() {
		return ""+getId();
	}
}
