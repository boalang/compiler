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

/**
 * Control flow graph builder node
 * @author ganeshau
 *
 */
public class CFGNode {
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
	private int type = TYPE_OTHER;
	private String pid;
	private Statement stmt;
	private Expression expr;

	public static HashMap<String, Integer> idOfLabel = new HashMap<String, Integer>();
	public static HashMap<Integer, String> labelOfID = new HashMap<Integer, String>();

	public HashSet<CFGEdge> inEdges = new HashSet<CFGEdge>();
	public HashSet<CFGEdge> outEdges = new HashSet<CFGEdge>();

	public CFGNode() {
		// TODO Auto-generated constructor stub
		this.id = ++numOfNodes;
		System.out.println();
	}

	public CFGNode(String label) {
		this.id = ++numOfNodes;
		this.label = label;
	}

	public CFGNode(String methodName, int type, String className,
			String objectName) {
		this.id = ++numOfNodes;
		this.methodId = convertLabel(methodName);
		this.type = type;
		this.classNameId = convertLabel(className);
		this.objectNameId = convertLabel(objectName);
	}

	public CFGNode(String methodName, int type, String className,
			String objectName, int numOfParameters, HashSet<Integer> datas) {
		this.id = ++numOfNodes;
		this.methodId = convertLabel(methodName);
		this.type = type;

		if (className == null) {
			this.classNameId = -1;
		} else {
			this.classNameId = convertLabel(className);
		}

		this.objectNameId = convertLabel(objectName);
		this.parameters = new HashSet<Integer>(datas);
		this.numOfParameters = numOfParameters;
	}

	public CFGNode(String methodName, int type, String className,
			String objectName, int numOfParameters) {
		this.id = ++numOfNodes;
		this.methodId = convertLabel(methodName);
		this.type = type;
		this.classNameId = convertLabel(className);
		this.objectNameId = convertLabel(objectName);
		this.numOfParameters = numOfParameters;
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

	public int getType() {
		return type;
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

	public boolean hasFalseBranch() {
		for (CFGEdge e : this.outEdges) {
			if (e.getLabel().equals("F"))
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

	public HashSet<CFGNode> getInNodes() {
		HashSet<CFGNode> nodes = new HashSet<CFGNode>();
		for (CFGEdge e : inEdges)
			nodes.add(e.getSrc());
		return nodes;
	}

	public HashSet<CFGNode> getOutNodes() {
		HashSet<CFGNode> nodes = new HashSet<CFGNode>();
		for (CFGEdge e : outEdges)
			nodes.add(e.getDest());
		return nodes;
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

	private String getName() {
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
		b.setType(getNodeType());
		if (this.stmt != null)
			b.setStatement(stmt);
		else if (this.expr != null)
			b.setExpression(expr);
		return b;
	}

	private CFGNodeType getNodeType() {
		switch (this.type) {
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
}
