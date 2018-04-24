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

import boa.graphs.cdg.CDG;
import boa.graphs.cdg.CDGEdge;
import boa.graphs.cdg.CDGNode;
import boa.graphs.cfg.CFG;
import boa.graphs.ddg.DDG;
import boa.graphs.ddg.DDGNode;
import boa.types.Ast.*;
import boa.types.Control;

import java.util.HashSet;
import java.util.Set;

/**
 * Program Dependence Graph builder
 *
 * @author marafat
 */

public class PDG {

    private PDGNode entryNode;
    private HashSet<PDGNode> nodes = new HashSet<PDGNode>();

    public PDG(final CDG cdg, final DDG ddg) {
        addCDG(cdg);
        addDDGEdges(ddg);
        entryNode = getNode(0);
    }

    public PDG(final CFG cfg) throws Exception {
        this(new CDG(cfg), new DDG(cfg));
    }

    public PDG(final Method m, boolean paramAsStatement) throws Exception {
        this(new CFG(m, paramAsStatement));
    }

    public PDG(final Method m) throws Exception {
        this(new CFG(m, false));
    }

    // Getters
    public PDGNode getEntryNode() {
        return entryNode;
    }

    public HashSet<PDGNode> getNodes() {
        return nodes;
    }

    /**
     * Gives back the node for the given node id, otherwise returns null
     *
     * @param id node id
     * @return
     */
    public PDGNode getNode(int id) {
        for (PDGNode n: nodes)
            if (n.getId() == id)
                return n;

        return null;
    }

    /**
     * Adds CDG nodes and edges to PDG
     *
     * @param cdg control dependence graph
     */
    private void addCDG(final CDG cdg) {
        for (CDGNode n: cdg.getNodes()) {
            PDGNode node = new PDGNode(n);
            nodes.add(node);
        }

        for (CDGNode n: cdg.getNodes()) {
            PDGNode node = getNode(n.getId());
            for (CDGNode s: n.getSuccessors())
                node.addSuccessor(getNode(s.getId()));
            for (CDGNode p: n.getPredecessors())
                node.addPredecessor(getNode(p.getId()));
            for (CDGEdge ie: n.getInEdges())
                node.addInEdge(new PDGEdge(getNode(ie.getSrc().getId()), getNode(ie.getDest().getId()), ie.getLabel(), Control.PDGEdge.PDGEdgeType.CONTROL));
            for (CDGEdge oe: n.getOutEdges())
                node.addOutEdge(new PDGEdge(getNode(oe.getSrc().getId()), getNode(oe.getDest().getId()), oe.getLabel(), Control.PDGEdge.PDGEdgeType.CONTROL));
        }

    }

    /**
     * Adds DDG edges to the PDG graph
     *
     * @param ddg data dependency graph
     */
    private void addDDGEdges(final DDG ddg) {
        for (DDGNode s: ddg.getDefUseChain().keySet()) {
            PDGNode src = getNode(s.getId());
            for (DDGNode d: ddg.getUseNodes(s)) {
                PDGNode dest = getNode(d.getId());
                src.addSuccessor(dest);
                dest.addPredecessor(src);
                PDGEdge e = new PDGEdge(src, dest, s.getDefVariable(), Control.PDGEdge.PDGEdgeType.DATA);
                src.addOutEdge(e);
                dest.addInEdge(e);
            }
        }
    }

}
