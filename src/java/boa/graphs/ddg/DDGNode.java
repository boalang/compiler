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

import java.util.HashSet;

/**
 * @author marafat
 */

public class DDGNode implements Comparable<DDGNode> {

    private int id;
    private Ast.Statement stmt;
    private Ast.Expression expr;

    private String defVariables;
    private HashSet<String> useVariables = new HashSet<String>();

//    private Set<CDGEdge> inEdges = new HashSet<CDGEdge>();
//    private Set<CDGEdge> outEdges = new HashSet<CDGEdge>();
//    private List<CDGNode> successors = new java.util.ArrayList<CDGNode>();
//    private List<CDGNode> predecessors = new java.util.ArrayList<CDGNode>();

    public DDGNode(final CFGNode node) {
        this.id = node.getId();
        this.stmt = node.getStmt();
        this.expr = node.getExpr();
        this.defVariables = node.defVariables;
        this.useVariables = node.useVariables;
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

    public void addUseVariable(final String useVariables) {
        this.useVariables.add(useVariables);
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

    public HashSet<String> getUseVariables() {
        return useVariables;
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
