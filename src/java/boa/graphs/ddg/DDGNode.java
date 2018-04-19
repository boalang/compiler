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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author marafat
 */

public class DDGNode implements Comparable<DDGNode> {

    private int id;
    private Ast.Statement stmt;
    private Ast.Expression expr;

    private String defVariables;
    private Set<String> useVariables = new HashSet<String>();

    private Set<DDGEdge> inEdges = new HashSet<DDGEdge>();
    private Set<DDGEdge> outEdges = new HashSet<DDGEdge>();
    private List<DDGNode> successors = new ArrayList<DDGNode>();
    private List<DDGNode> predecessors = new ArrayList<DDGNode>();

    public DDGNode(final CFGNode node) {
        this.id = node.getId();
        this.stmt = node.getStmt();
        this.expr = node.getExpr();
        this.defVariables = node.defVariables;
        this.useVariables = node.useVariables;
    }

    public DDGNode(int id) {
        this.id = id;
    }

    //Setters
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
        this.defVariables = defVariables;
    }

    public void setUseVariables(final Set<String> useVariables) {
        this.useVariables = useVariables;
    }

    public void addUseVariable(final String useVariables) {
        this.useVariables.add(useVariables);
    }

    public void addOutEdge(DDGEdge edge) {
        outEdges.add(edge);
    }

    public void addinEdge(DDGEdge edge) {
        inEdges.add(edge);
    }

    public void addSuccessor(DDGNode node) {
        successors.add(node);
    }

    public void addPredecessor(DDGNode node) {
        predecessors.add(node);
    }

    //Getters
    public int getId() {
        return id;
    }

    public Ast.Statement getStmt() {
        return stmt;
    }

    public Ast.Expression getExpr() {
        return expr;
    }

    public String getDefVariable() {
        return defVariables;
    }

    public Set<String> getUseVariables() {
        return useVariables;
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
