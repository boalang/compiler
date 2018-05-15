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

import boa.graphs.cfg.CFG;
import boa.graphs.cfg.CFGNode;
import boa.types.Ast.*;

import java.util.*;

/**
 * Data Dependence Graph builder
 *
 * @author marafat
 * @author rdyer
 */
public class DDG {
    private Method md;
    private DDGNode entryNode;
    private final HashSet<DDGNode> nodes = new HashSet<DDGNode>();
    private final HashMap<DDGNode, Set<DDGNode>> defUseChain = new HashMap<DDGNode, Set<DDGNode>>();
    //private HashMap<DDGNode, Set<DDGNode>> useDefChain; //TODO: needs reaching-def analysis

    /**
     * Constructs a data dependence graph
     *
     * @param cfg control flow graph
     * @throws Exception if DDG construction fails
     */
    public DDG(final CFG cfg) throws Exception {
        this.md = cfg.md;
        if (cfg != null && cfg.getNodes().size() > 0) {
            final Map<Integer, InOut> liveVars = getLiveVariables(cfg);
            formDefUseChains(liveVars, cfg);
            constructDDG(liveVars.keySet());
        }
    }

    /**
     * Constructs a data dependence graph
     *
     * @param md method whose DDG is to be built
     * @param paramAsStatement if true, inserts parameters as assign statements at the
     *                         begining of control flow graph. Default is set to false
     * @throws Exception if DDG construction fails
     */
    public DDG(final Method md, boolean paramAsStatement) throws Exception {
        this(new CFG(md, paramAsStatement).get());
    }

    /**
     * Constructs a data dependence graph
     *
     * @param md method whose DDG is to be built
     * @throws Exception if DDG construction fails
     */
    public DDG(final Method md) throws Exception {
        this(new CFG(md).get());
    }

    /**
     * Returns the method whose DDG is built
     *
     * @return the method whose DDG is built
     */
    public Method getMethod() {
        return md;
    }

    /**
     * Returns the entry node to the graph
     *
     * @return the entry node to the graph
     */
    public DDGNode getEntryNode() { return  entryNode; }

    /**
     * Returns the set of all the nodes in the graph
     *
     * @return the set of all the nodes in the graph
     */
    public HashSet<DDGNode> getNodes() { return nodes; }

    /**
     * Returns the map of definiton-use chains
     *
     * @return the map of definiton-use chains
     */
    public HashMap<DDGNode, Set<DDGNode>> getDefUseChain() {
        return defUseChain;
    }

    private Set<DDGNode> getUseNodes(final DDGNode node) {
        return defUseChain.get(node);
    }

    /**
     * Gives back all the def nodes for the given variable
     *
     * @param var variable
     * @return definition nodes
     */
    public Set<DDGNode> getDefNodes(final String var) {
        final Set<DDGNode> defNodes = new HashSet<DDGNode>();
        for (final DDGNode n : defUseChain.keySet()) {
            if (n.getDefVariable().equals(var))
                defNodes.add(n);
        }

        return defNodes;
    }

    /**
     * Returns the DDG node for the given node id. If not found then returns null
     *
     * @param id node id
     * @return DDGNode
     */
    public DDGNode getNode(final int id) {
        for (final DDGNode n : nodes)
            if (n.getId() == id)
                return n;

        return null;
    }

    /**
     * Computes and returns a map of in and out variables for each node
     *
     * @param cfg control flow graph
     * @return map of in and out variables for each node
     * @throws Exception
     */
    private Map<Integer, InOut> getLiveVariables(final CFG cfg) {
        // initialize
        final Map<Integer, InOut> liveVars = new HashMap<Integer, InOut>();
        for (final CFGNode n : cfg.getNodes())
            liveVars.put(n.getId(), new InOut());

        final Map<Integer, InOut> currentLiveVars = new HashMap<Integer, InOut>();
        final int stopid = cfg.getNodes().size() - 1;
        boolean saturated = false;

        while (!saturated) { // fix point iteration
            int changeCount = 0;

            for (final CFGNode node : cfg.getNodes()) {
                final InOut nodeLiveVars = new InOut(liveVars.get(node.getId()));

                // out = Union in[node.successor]
                for (final CFGNode s : node.getSuccessorsList())
                    nodeLiveVars.out.addAll(liveVars.get(s.getId()).in);

                // out - def
                final Set<Pair> diff = new HashSet<Pair>(nodeLiveVars.out);
                if (!node.getDefVariables().equals(""))
                    for (final Pair p : nodeLiveVars.out)
                        if (p.var.equals(node.getDefVariables()))
                            diff.remove(p);

                // in = use Union (out - def)
                for (final String var : node.getUseVariables())
                    nodeLiveVars.in.add(new Pair(var, node));
                nodeLiveVars.in.addAll(diff);

                // check if node's "in" has changed
                diff.clear();
                diff.addAll(nodeLiveVars.in);
                diff.removeAll(liveVars.get(node.getId()).in);
                if (diff.size() > 0)
                    changeCount++;

                // check if node's "out" has changed
                diff.clear();
                diff.addAll(nodeLiveVars.out);
                diff.removeAll(liveVars.get(node.getId()).out);
                if (diff.size() > 0)
                    changeCount++;

                currentLiveVars.put(node.getId(), nodeLiveVars);
            }

            if (changeCount == 0) {
                saturated = true;
            } else {
                liveVars.clear();
                liveVars.putAll(currentLiveVars);
                currentLiveVars.clear();
            }
        }

        liveVars.remove(stopid);
        return liveVars;
    }

    /**
     * Forms def-use chains to establish data flow between nodes
     *
     * @param liveVar map of nodes and their in and out variables
     */
    private void formDefUseChains(final Map<Integer, InOut> liveVar, CFG cfg) {
        // match def variable of the node with the out variable. If the match occurs form a def-use mapping
        for (final Map.Entry<Integer, InOut> entry: liveVar.entrySet()) {
            final CFGNode n = cfg.getNode(entry.getKey());
            final DDGNode defNode = getNode(n);
            if (entry.getKey() != 0) {
                for (final Pair p : entry.getValue().out) {
                    if (!n.getDefVariables().equals("")) {
                        if (n.getDefVariables().equals(p.var)) {
                            final DDGNode useNode = getNode(p.node);
                            if (!defUseChain.containsKey(defNode))
                                defUseChain.put(defNode, new HashSet<DDGNode>());
                            defUseChain.get(defNode).add(useNode);
                            // connect nodes for constructing the graph
                            defNode.addSuccessor(useNode);
                            useNode.addPredecessor(defNode);
                            final DDGEdge edge = new DDGEdge(defNode, useNode, p.var);
                            defNode.addOutEdge(edge);
                            useNode.addinEdge(edge);
                        }
                    }
                }
            }
        }
    }

    /**
     * Connects the disconnected nodes to form the DDG graph
     *
     * @param nodeids set of all node ids of the graph
     */
    private void constructDDG(final Set<Integer> nodeids) {
        // any node without parent is connected to entryNode
        entryNode = getNode(0);
        for (final int i : nodeids) {
            if (i != 0) {
                final DDGNode dest = getNode(i);
                if (dest.getPredecessors().size() == 0 ||
                        (dest.getPredecessors().size() == 1 && dest.getPredecessors().get(0).equals(dest))) {
                    entryNode.addSuccessor(dest);
                    dest.addPredecessor(entryNode);
                    final DDGEdge edge = new DDGEdge(entryNode, dest);
                    entryNode.addOutEdge(edge);
                    dest.addinEdge(edge);
                }
            }
        }
    }

    /**
     * Returns the existing DDG node for the given Tree node. If not found then returns a new node
     *
     * @param cfgNode control flow graph node
     * @return the existing DDG node for the given Tree node. If not found then returns a new node
     */
    private DDGNode getNode(final CFGNode cfgNode) {
        final DDGNode node = getNode(cfgNode.getId());
        if (node != null)
            return node;

        final DDGNode newNode = new DDGNode(cfgNode);
        nodes.add(newNode);
        return newNode;
    }

    // Holds in and out pairs for each node
    private static class InOut {
        Set<Pair> in;
        Set<Pair> out;

        InOut() {
            in = new HashSet<Pair>();
            out = new HashSet<Pair>();
        }

        InOut(final HashSet<Pair> in, final HashSet<Pair> out){
            this.in = in;
            this.out = out;
        }

        InOut(final InOut inout){
            this.in = new HashSet<Pair>(inout.in);
            this.out = new HashSet<Pair>(inout.out);
        }

        public InOut clone() {
            return new InOut(this);
        }
    }

    // (Var, Usenode) pairs: use nodes are needed to construct def-use chains
    private static class Pair {
        String var;
        CFGNode node;

        Pair() {}

        Pair(final String var, final CFGNode node) {
            this.var = var;
            this.node = node;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Pair pair = (Pair) o;

            return var.equals(pair.var) && node.getId() == pair.node.getId();
        }

        @Override
        public int hashCode() {
            int result = var.hashCode();
            result = 31 * result + node.hashCode();
            return result;
        }
    }
}
