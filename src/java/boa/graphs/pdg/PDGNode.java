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
package boa.graphs.pdg;

import boa.graphs.cdg.CDGNode;
import boa.types.Ast.*;
import boa.types.Control;
import boa.types.Control.PDGNode.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Program Dependence Graph builder node
 *
 * @author marafat
 */

public class PDGNode implements Comparable<PDGNode> {

    private int id;
    private Statement stmt;
    private Expression expr;
    private PDGNodeType kind = PDGNodeType.OTHER;

    private List<PDGNode> successors = new ArrayList<PDGNode>();
    private List<PDGNode> predecessors = new ArrayList<PDGNode>();
    private Set<PDGEdge> inEdges = new HashSet<PDGEdge>();
    private Set<PDGEdge> outEdges = new HashSet<PDGEdge>();

    public PDGNode(final CDGNode node) {
        this.id = node.getId();
        this.stmt = node.getStmt();
        this.expr = node.getExpr();
        this.kind = convertKind(node.getKind());
    }

    public PDGNode(int id) {
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

    public void setKind(final PDGNodeType kind) {
        this.kind = kind;
    }

    public void setKind(final Control.CDGNode.CDGNodeType kind) {
        this.kind = convertKind(kind);
    }

    public void addInEdge(final PDGEdge inEdges) {
        this.inEdges.add(inEdges);
    }

    public void addOutEdge(final PDGEdge outEdges) {
        this.outEdges.add(outEdges);
    }

    public void addSuccessor(final PDGNode node) {
        if (!successors.contains(node))
            successors.add(node);
    }

    public void addPredecessor(final PDGNode node) {
        if (!predecessors.contains(node))
            predecessors.add(node);
    }

    //Getters
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

    public PDGNodeType getKind() {
        return kind;
    }

    public Set<PDGEdge> getInEdges() {
        return inEdges;
    }

    public Set<PDGEdge> getOutEdges() {
        return outEdges;
    }

    public List<PDGNode> getSuccessors() {
        return successors;
    }

    public List<PDGNode> getPredecessors() {
        return predecessors;
    }

    /**
     * Gives back equivalent PDG node type
     *
     * @param type CDG node type
     * @return PDGNodeType
     */
    public PDGNodeType convertKind(final Control.CDGNode.CDGNodeType type) {
        switch(type) {
            case ENTRY:
                return PDGNodeType.ENTRY;
            case OTHER:
                return PDGNodeType.OTHER;
            case METHOD:
                return PDGNodeType.METHOD;
            case CONTROL:
                return PDGNodeType.CONTROL;
        }

        return null;
    }


    @Override
    public int compareTo(final PDGNode node) {
        return node.id - this.id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PDGNode pdgNode = (PDGNode) o;

        return id == pdgNode.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
