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
package boa.graphs.trees;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import boa.types.Ast.Statement;
import boa.types.Ast.Expression;
import boa.graphs.cfg.CFGNode;
import boa.types.Control;
import boa.types.Control.TreeNode.*;

/**
 * Tree builder node
 *
 * @author marafat
 */

public class TreeNode implements Comparable<TreeNode> {

    private int id;
    private TreeNode parent;
    private Statement stmt;
    private Expression expr;
    private TreeNodeType kind = TreeNodeType.OTHER;

    private String defVariable;
    private HashSet<String> useVariables = new HashSet<String>();

    private ArrayList<TreeNode> children = new ArrayList<TreeNode>();

    public TreeNode(CFGNode node) {
        this.id = node.getId();
        this.stmt = node.getStmt();
        this.expr = node.getExpr();
        this.kind = convertKind(node.getKind());
        this.defVariable = node.getDefVariables();
        this.useVariables = node.getUseVariables();
    }

    public TreeNode(int id) {
        this.id = id;
    }

    // Setters
    public void setParent(final TreeNode parent) {
        this.parent = parent;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStmt(final Statement stmt) {
        this.stmt = stmt;
    }

    public void setExpr(final Expression expr) {
        this.expr = expr;
    }

    public void setKind(final TreeNodeType kind) { this.kind = kind; }

    public String getDefVariable() {
        return defVariable;
    }

    public HashSet<String> getUseVariables() {
        return useVariables;
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

    public void setKind(final Control.CFGNode.CFGNodeType kind) {
        this.kind = convertKind(kind);
    }

    public void addChild(final TreeNode node) {
        if (!children.contains(node))
            children.add(node);
    }

    // Getters
    public TreeNode getParent() {
        return parent;
    }

    public int getId() {
        return id;
    }

    public Statement getStmt() {
        return stmt;
    }

    public Expression getExpr() {
        return expr;
    }

    public TreeNodeType getKind() {
        return kind;
    }

    public ArrayList<TreeNode> getChildren() {
        return children;
    }

    /**
     * Gives back equivalent Tree node type
     *
     * @param type CFG node type
     * @return TreeNodeType
     */
    public TreeNodeType convertKind(final Control.CFGNode.CFGNodeType type) {
        switch(type) {
            case ENTRY:
                return TreeNodeType.ENTRY;
            case OTHER:
                return TreeNodeType.OTHER;
            case METHOD:
                return TreeNodeType.METHOD;
            case CONTROL:
                return TreeNodeType.CONTROL;
        }

        return null;
    }

    @Override
    public int compareTo(final TreeNode node) {
        return node.id - this.id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeNode treeNode = (TreeNode) o;

        return id == treeNode.id;
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