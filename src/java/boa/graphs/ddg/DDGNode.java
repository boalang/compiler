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

import java.util.HashSet;
import java.util.Set;

import boa.graphs.Node;
import boa.graphs.cfg.CFGNode;

/**
 * Data Dependence Graph builder node
 *
 * @author marafat
 * @author rdyer
 */
public class DDGNode extends Node<DDGNode, DDGEdge> {
    protected String defVariable;
    protected HashSet<String> useVariables = new HashSet<String>();

    /**
     * Constructs a DDG node.
     *
     * @param node control flow graph node
     */
    public DDGNode(final CFGNode node) {
        this(node.getNodeId());
        this.stmt = node.getStmt();
        this.expr = node.getExpr();
        this.kind = node.getKind();
        this.defVariable = node.getDefVariables();
        this.useVariables = node.getUseVariables();
    }

    /**
     * Constructs a CDG node.
     *
     * @param id node id. Uses default values for remaining fields
     */
    public DDGNode(int id) {
        this.id = id;
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

    public String getDefVariable() {
        return this.defVariable;
    }

    public Set<String> getUseVariables() {
        return this.useVariables;
    }

    @Override
    public String toString() {
        return "" + id;
    }
}
