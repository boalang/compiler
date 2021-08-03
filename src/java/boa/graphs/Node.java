/*
 * Copyright 2018, Robert Dyer,
 *                 Bowling Green State University
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
package boa.graphs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import boa.types.Ast.Expression;
import boa.types.Ast.Statement;
import boa.types.Control.Node.Builder;
import boa.types.Control.Node.NodeType;

/**
 * A graph node
 *
 * @author rdyer
 */
public abstract class Node<N extends Node<N, E>, E extends Edge<N, E>> implements Comparable<N> {
	public static int numOfNodes = -1;

	protected int id;
	protected NodeType kind = NodeType.OTHER;
	protected String pid;
	protected Statement stmt;
	protected Expression expr;

	protected final Set<E> inEdges = new HashSet<E>();
	protected final Set<E> outEdges = new HashSet<E>();

	@Override
	public int compareTo(final N node) {
		return node.id - this.id;
	}

	public Node() {
		this.id = ++numOfNodes;
	}

	public Node(final NodeType kind) {
		this.id = ++numOfNodes;
		this.kind = kind;
	}

	public Statement getStmt() {
		return this.stmt;
	}

	public void setStmt(final Statement stmt) {
		this.stmt = stmt;
	}

	public boolean hasStmt() {
		return this.stmt != null;
	}

	public Expression getExpr() {
		return this.expr;
	}

	public void setExpr(final Expression expr) {
		this.expr = expr;
	}

	public boolean hasExpr() {
		return this.expr != null;
	}

	public long getId() {
		return (long)this.id;
	}

	public int getNodeId() {
		return this.id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public Set<E> getInEdges() {
		return this.inEdges;
	}

	public Set<E> getOutEdges() {
		return this.outEdges;
	}

	public List<N> getPredecessorsList() {
		return getPredecessors();
	}

	public List<N> getSuccessorsList() {
		return getSuccessors();
	}

	public List<N> getPredecessors() {
		final Set<N> nodes = new HashSet<N>();
		for (final E e : this.inEdges)
			nodes.add(e.getSrc());
		return new ArrayList<N>(nodes);
	}

	public List<N> getSuccessors() {
		final Set<N> nodes = new HashSet<N>();
		for (final E e : this.outEdges)
			nodes.add(e.getDest());
		return new ArrayList<N>(nodes);
	}

	public E getOutEdge(final N node) {
		for (final E e : this.outEdges) {
			if (e.getDest() == node)
				return e;
		}
		return null;
	}

	public E getInEdge(final N node) {
		for (final E e : this.inEdges) {
			if (e.getSrc() == node)
				return e;
		}
		return null;
	}

	public void addInEdge(final E edge) {
		if (getInEdge(edge.getSrc()) == null)
			this.inEdges.add(edge);
	}

	public void addOutEdge(final E edge) {
		if (getOutEdge(edge.getDest()) == null)
			this.outEdges.add(edge);
	}

	public void removeOutEdge(final E edge) {
		this.outEdges.remove(edge);
	}

	public void removeInEdge(final E edge) {
		this.inEdges.remove(edge);
	}

	public void delete() {
		final List<E> edges = new ArrayList<E>();
		edges.addAll(inEdges);
		edges.addAll(outEdges);
		for (final Edge<?, ?> e : edges)
			e.delete();
	}

	public String getPid() {
		return this.pid;
	}

	public void setPid(final String pid) {
		this.pid = pid;
	}

	public void setAstNode(final Statement stmt) {
		this.stmt = stmt;
	}

	public void setAstNode(final Expression expr) {
		this.expr = expr;
	}

	public NodeType getKind() {
		return this.kind;
	}

	public void setKind(final NodeType kind) {
		this.kind = kind;
	}

	public String getName() {
		return "";
	}

	public Builder newBuilder() {
		final Builder b = boa.types.Control.Node.newBuilder();
		b.setId(this.id);
		b.setKind(this.kind);
		if (this.stmt != null)
			b.setStatement(this.stmt);
		else if (this.expr != null)
			b.setExpression(this.expr);
		return b;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof Node)) return false;

		final Node<?, ?> n = (Node<?, ?>) o;

		return id == n.id;
	}

	private int hash = -1;

	@Override
	public int hashCode() {
		if (hash == -1) {
			hash = 1;
			hash = hash * 31 + id;
		}
		return hash;
	}

	public String toString() {
		return "node " + getNodeId();
	}
}
