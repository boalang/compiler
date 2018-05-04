/*
 * Copyright 2018, Robert Dyer, Mohd Arafat
 *                 and Bowling Green State University
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
package boa.graphs.cdg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import boa.graphs.trees.TreeNode;
import boa.types.Ast.Statement;
import boa.types.Ast.Expression;
import boa.types.Control;
import boa.types.Control.CDGNode.*;

/**
 * Control Dependence Graph node
 *
 * @author marafat
 */

public class CDGNode implements Comparable<CDGNode> {

    private int id;
    private Statement stmt;
    private Expression expr;
    private CDGNodeType kind = CDGNodeType.OTHER;

    private String defVariable;
    private HashSet<String> useVariables = new HashSet<String>();

    private ArrayList<CDGNode> successors = new ArrayList<CDGNode>();
    private ArrayList<CDGNode> predecessors = new ArrayList<CDGNode>();
    private HashSet<CDGEdge> inEdges = new HashSet<CDGEdge>();
    private HashSet<CDGEdge> outEdges = new HashSet<CDGEdge>();

    // Constructors

    /**
     * Constructs a CDG node.
     *
     * @param node Tree node
     */

    public CDGNode(final TreeNode node) {
        this.id = node.getId();
        this.stmt = node.getStmt();
        this.expr = node.getExpr();
        this.kind = convertKind(node.getKind());
        this.defVariable = node.getDefVariable();
        this.useVariables = node.getUseVariables();
    }

    /**
     * Constructs a CDG node.
     *
     * @param id node id. Uses default values for remaining fields
     */
    public CDGNode(int id) {
        this.id = id;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setStmt(final Statement stmt) {
        this.stmt = stmt;
    }

    public void setExpr(final Expression expr) {
        this.expr = expr;
    }

    public void setKind(final CDGNodeType kind) {
        this.kind = kind;
    }

    public void setKind(final Control.TreeNode.TreeNodeType kind) {
        this.kind = convertKind(kind);
    }

    public void setDefVariable(final String defVariables) {
        this.defVariable = defVariables;
    }

    public void setUseVariables(final HashSet<String> useVariables) {
        this.useVariables = useVariables;
    }

    public void addUseVariable(final String useVariables) {
        this.useVariables.add(useVariables);
    }

    public void addSuccessor(final CDGNode node) {
        successors.add(node);
    }

    public void addPredecessor(final CDGNode node) {
        predecessors.add(node);
    }

    public void addInEdge(final CDGEdge edge) {
        inEdges.add(edge);
    }

    public void addOutEdge(final CDGEdge edge) {
        outEdges.add(edge);
    }

    // Getters
    public int getId() {
        return id;
    }

    public boolean hasStmt() { return this.stmt != null; }

    public Statement getStmt() {
        return stmt;
    }

    public boolean hasExpr() {
        return this.expr != null;
    }

    public Expression getExpr() {
        return expr;
    }

    public Set<CDGEdge> getInEdges() {
        return inEdges;
    }

    public CDGNodeType getKind() {
        return kind;
    }

    public String getDefVariable() {
        return defVariable;
    }

    public HashSet<String> getUseVariables() {
        return useVariables;
    }

    public List<CDGNode> getSuccessors() {
        return successors;
    }

    public List<CDGNode> getPredecessors() {
        return predecessors;
    }

    public Set<CDGEdge> getOutEdges() {
        return outEdges;
    }

    public CDGEdge getInEdge(final CDGNode node) {
        for (final CDGEdge e : this.inEdges) {
            if (e.getSrc().equals(node))
                return e;
        }
        return null;
    }

    public CDGEdge getOutEdge(final CDGNode node) {
        for (final CDGEdge e : this.outEdges) {
            if (e.getDest().equals(node))
                return e;
        }
        return null;
    }

    /**
     * Returns equivalent CDG node type for the given Tree node type
     *
     * @param type Tree node type
     * @return CDG node type for the given Tree node type
     */
    public CDGNodeType convertKind(final Control.TreeNode.TreeNodeType type) {
        switch(type) {
            case ENTRY:
                return CDGNodeType.ENTRY;
            case OTHER:
                return CDGNodeType.OTHER;
            case METHOD:
                return CDGNodeType.METHOD;
            case CONTROL:
                return CDGNodeType.CONTROL;
        }

        return null;
    }

    @Override
    public int compareTo(final CDGNode node) {
        return node.id - this.id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CDGNode cdgNode = (CDGNode) o;

        return id == cdgNode.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "" + id;
    }
}
