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

import boa.graphs.Edge;
import boa.graphs.Node;
import boa.graphs.trees.TreeNode;
import boa.types.Ast.Statement;
import boa.types.Ast.Expression;
import boa.types.Control.Node.*;

/**
 * Control Dependence Graph node
 *
 * @author marafat
 * @author rdyer
 */
public class CDGNode extends Node<CDGNode, CDGEdge> {
    private String defVariable;
    private Set<String> useVariables;

    /**
     * Constructs a CDG node.
     *
     * @param node Tree node
     */
    public CDGNode(final TreeNode node) {
        this.id = node.getNodeId();
        this.stmt = node.getStmt();
        this.expr = node.getExpr();
        this.kind = node.getKind();
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
        this.useVariables = new HashSet<String>();
    }

    public String getDefVariable() {
        return defVariable;
    }

    public void setDefVariable(final String defVariables) {
        this.defVariable = defVariables;
    }

    public Set<String> getUseVariables() {
        return useVariables;
    }

    public void setUseVariables(final HashSet<String> useVariables) {
        this.useVariables = useVariables;
    }

    @Override
    public String toString() {
        return "" + id;
    }
}
