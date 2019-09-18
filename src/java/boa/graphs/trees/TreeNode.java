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

import boa.graphs.Node;
import boa.graphs.cfg.CFGNode;

/**
 * Tree node
 *
 * @author marafat
 */
public class TreeNode extends Node<TreeNode, TreeEdge> {
    private TreeNode parent;

    private String defVariable;
    private HashSet<String> useVariables;

    private final ArrayList<TreeNode> children = new ArrayList<TreeNode>();

    /**
     * Constructs a tree node.
     *
     * @param node CFG node
     */
    public TreeNode(final CFGNode node) {
        this.id = node.getNodeId();
        this.stmt = node.getStmt();
        this.expr = node.getExpr();
        this.kind = node.getKind();
        this.defVariable = node.getDefVariables();
        this.useVariables = node.getUseVariables();
    }

    /**
     * Constructs a tree node.
     *
     * @param id node id. Uses default values for remaining fields
     */
    public TreeNode(final int id) {
        this.id = id;
        this.useVariables = new HashSet<String>();
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(final TreeNode parent) {
        this.parent = parent;
    }

    public String getDefVariable() {
        return defVariable;
    }

    public Set<String> getUseVariables() {
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

    public void addChild(final TreeNode node) {
        if (!children.contains(node))
            children.add(node);
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "" + id;
    }
}
