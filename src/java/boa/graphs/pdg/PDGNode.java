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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import boa.graphs.Node;
import boa.graphs.cdg.CDGNode;

/**
 * Program Dependence Graph node
 *
 * @author marafat
 * @author rdyer
 */
public class PDGNode extends Node<PDGNode, PDGEdge> {
    private String defVariable;
    private Set<String> useVariables = new HashSet<String>();

    /**
     * Constructs a PDG node.
     *
     * @param node control dependence graph node
     */
    public PDGNode(final CDGNode node) {
        this.id = node.getNodeId();
        this.stmt = node.getStmt();
        this.expr = node.getExpr();
        this.kind = node.getKind();
        this.defVariable = node.getDefVariable();
        this.useVariables = node.getUseVariables();
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
        return defVariable;
    }

    public Set<String> getUseVariables() {
        return useVariables;
    }

    /**
     * Returns list of out edges with control edges first followed by data edges.
     *
     * @param node destination node
     * @return list of out edges
     */
    public List<PDGEdge> getOutEdges(final PDGNode node) {
        final List<PDGEdge> edges = new ArrayList<PDGEdge>();
        int pos = 0;
        for (final PDGEdge e : outEdges) {
            if (e.getDest().equals(node))
                if (e.getKind() == boa.types.Control.Edge.EdgeType.CONTROL) {
                    edges.add(pos, e);
                    pos++;
                } else {
                    edges.add(e);
                }
        }
        return edges;
    }

    @Override
    public String toString() {
        return "" + id;
    }
}
