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
package boa.graphs.ddg;

import boa.graphs.cfg.CFGNode;
import boa.types.Ast;
import boa.types.Control;
import boa.types.Control.DDGNode.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data Dependence Graph builder node
 *
 * @author marafat
 */

public class DDGNode implements Comparable<DDGNode> {

    private int id;
    private Ast.Statement stmt;
    private Ast.Expression expr;
    private DDGNodeType kind = DDGNodeType.OTHER;

    private String defVariable;
    private Set<String> useVariables = new HashSet<String>();

    private Set<DDGEdge> inEdges = new HashSet<DDGEdge>();
    private Set<DDGEdge> outEdges = new HashSet<DDGEdge>();
    private List<DDGNode> successors = new ArrayList<DDGNode>();
    private List<DDGNode> predecessors = new ArrayList<DDGNode>();

    public DDGNode(final CFGNode node) {
        this.id = node.getId();
        this.stmt = node.getStmt();
        this.expr = node.getExpr();
        this.kind = convertKind(node.getKind());
        this.defVariable = node.getDefVariables();
        this.useVariables = node.getUseVariables();
    }

    public DDGNode(int id) {
        this.id = id;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setStmt(final Ast.Statement stmt) {
        this.stmt = stmt;
    }

    public void setExpr(final Ast.Expression expr) {
        this.expr = expr;
    }

    public void setDefVariable(final String defVariables) {
        this.defVariable = defVariables;
    }

    public void setUseVariables(final Set<String> useVariables) {
        this.useVariables = useVariables;
    }

    public void setKind(final DDGNodeType kind) {
        this.kind = kind;
    }

    public void setKind(final Control.CFGNode.CFGNodeType kind) {
        this.kind = convertKind(kind);
    }

    public void addUseVariable(final String useVariables) {
        this.useVariables.add(useVariables);
    }

    public void addOutEdge(final DDGEdge edge) {
        outEdges.add(edge);
    }

    public void addinEdge(final DDGEdge edge) {
        inEdges.add(edge);
    }

    public void addSuccessor(final DDGNode node) {
        successors.add(node);
    }

    public void addPredecessor(final DDGNode node) {
        predecessors.add(node);
    }

    // Getters
    public int getId() {
        return id;
    }

    public boolean hasStmt() { return this.stmt != null; }

    public Ast.Statement getStmt() {
        return stmt;
    }

    public boolean hasExpr() {
        return this.expr != null;
    }

    public Ast.Expression getExpr() {
        return expr;
    }

    public String getDefVariable() {
        return defVariable;
    }

    public Set<String> getUseVariables() {
        return useVariables;
    }

    public DDGNodeType getKind() {
        return kind;
    }

    public Set<DDGEdge> getInEdges() {
        return inEdges;
    }

    public Set<DDGEdge> getOutEdges() {
        return outEdges;
    }

    public List<DDGNode> getSuccessors() {
        return successors;
    }

    public List<DDGNode> getPredecessors() {
        return predecessors;
    }

    /**
     * Gives back equivalent DDG node type
     *
     * @param type CFG node type
     * @return DDGNodeType
     */
    public DDGNodeType convertKind(final Control.CFGNode.CFGNodeType type) {
        switch(type) {
            case ENTRY:
                return DDGNodeType.ENTRY;
            case OTHER:
                return DDGNodeType.OTHER;
            case METHOD:
                return DDGNodeType.METHOD;
            case CONTROL:
                return DDGNodeType.CONTROL;
        }

        return null;
    }

    @Override
    public int compareTo(final DDGNode node) {
        return node.id - this.id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DDGNode ddgNode = (DDGNode) o;

        return id == ddgNode.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
