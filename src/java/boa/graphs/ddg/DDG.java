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
        if (cfg != null && cfg.getNodes().size() > 0) {
            this.md = cfg.getMd();
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

    public DDGNode[] sortNodes() {
        try {
            final DDGNode[] results = new DDGNode[nodes.size()];
            for (final DDGNode node : nodes) {
                results[node.getNodeId()] = node;
            }
            return results;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the map of definiton-use chains
     *
     * @return the map of definiton-use chains
     */
    public HashMap<DDGNode, Set<DDGNode>> getDefUseChain() {
        return defUseChain;
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
            if (n.getDefVariable() != null && n.getDefVariable().equals(var))
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
            if (n.getNodeId() == id)
                return n;

        return null;
    }

    /**
     * Computes and returns a map of in and out variables for each node
     *
     * @param cfg control flow graph
     * @return map of in and out variables for each node
     */
    private Map<Integer, InOut> getLiveVariables(final CFG cfg) {
        Map<Integer, BitSet> liveVarsIn = new HashMap<Integer, BitSet>();
        Map<Integer, BitSet> currentLiveVarsIn = new HashMap<Integer, BitSet>();
        Map<Integer, BitSet> liveVarsOut = new HashMap<Integer, BitSet>();
        Map<Integer, BitSet> currentLiveVarsOut = new HashMap<Integer, BitSet>();

        final CFGNode[] cfgNodes = cfg.reverseSortNodes();
        final Map<CFGNode, List<CFGNode>> successors = new HashMap<CFGNode, List<CFGNode>>();
        final Map<Integer, BitSet> nodeUsePairs = new HashMap<Integer, BitSet>();
        final Map<Integer, Pair> pairMap = new HashMap<Integer, Pair>();

        for (final CFGNode n : cfgNodes) {
            liveVarsIn.put(n.getNodeId(), new BitSet());
            liveVarsOut.put(n.getNodeId(), new BitSet());
            successors.put(n, n.getSuccessors());

            // cache Pair's of use variables for every node
            final BitSet l = new BitSet();
            for (final String var : n.getUseVariables()) {
                final Pair p = new Pair(var, n);
                l.set(pairMap.size());
                pairMap.put(pairMap.size(), p);
            }
            nodeUsePairs.put(n.getNodeId(), l);
        }

        while (true) { // fix point iteration
            boolean changed = false;

            for (final CFGNode node : cfgNodes) {
                final BitSet nodeLiveVarsIn = (BitSet)liveVarsIn.get(node.getNodeId()).clone();
                final BitSet nodeLiveVarsOut = (BitSet)liveVarsOut.get(node.getNodeId()).clone();

                // out = Union in[node.successor]
                for (final CFGNode s : successors.get(node))
                    nodeLiveVarsOut.or(liveVarsIn.get(s.getNodeId()));

                // out - def
                final BitSet diff = (BitSet)nodeLiveVarsOut.clone();
                if (!node.getDefVariables().equals(""))
                    for (int p = 0; p < nodeLiveVarsOut.size(); p++)
                        if (nodeLiveVarsOut.get(p))
                            if (pairMap.get(p).var.equals(node.getDefVariables()))
                                diff.clear(p);

                // in = use Union (out - def)
                nodeLiveVarsIn.or(nodeUsePairs.get(node.getNodeId()));
                nodeLiveVarsIn.or(diff);

                // check if node's "in" or "out" have changed
                if (!nodeLiveVarsIn.equals(liveVarsIn.get(node.getNodeId())) || !nodeLiveVarsOut.equals(liveVarsOut.get(node.getNodeId())))
                    changed = true;

                currentLiveVarsIn.put(node.getNodeId(), nodeLiveVarsIn);
                currentLiveVarsOut.put(node.getNodeId(), nodeLiveVarsOut);
            }

            if (!changed)
                break;

            liveVarsIn = currentLiveVarsIn;
            currentLiveVarsIn = new HashMap<Integer, BitSet>();
            liveVarsOut = currentLiveVarsOut;
            currentLiveVarsOut = new HashMap<Integer, BitSet>();
        }

        liveVarsIn.remove(cfg.getNodes().size() - 1);
        liveVarsOut.remove(cfg.getNodes().size() - 1);

        final Map<Integer, InOut> liveVars = new HashMap<Integer, InOut>();
        for (final Integer i : liveVarsIn.keySet()) {
            final Set<Pair> ins = new LinkedHashSet<Pair>();
            final Set<Pair> outs = new LinkedHashSet<Pair>();
            for (int j = 0; j < liveVarsIn.get(i).size(); j++)
                if (liveVarsIn.get(i).get(j))
                    ins.add(pairMap.get(j));
            for (int j = 0; j < liveVarsOut.get(i).size(); j++)
                if (liveVarsOut.get(i).get(j))
                    outs.add(pairMap.get(j));
            liveVars.put(i, new InOut(ins, outs));
        }

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
                            new DDGEdge(defNode, useNode, p.var);
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
                if (dest.getPredecessors().size() == 0
                        || (dest.getPredecessors().size() == 1 && dest.getPredecessors().get(0).equals(dest))) {
                    new DDGEdge(entryNode, dest);
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
        final DDGNode node = getNode(cfgNode.getNodeId());
        if (node != null)
            return node;

        final DDGNode newNode = new DDGNode(cfgNode);
        nodes.add(newNode);
        return newNode;
    }

    /**
     * Holds in and out sets for each node
     */
    private static class InOut {
        Set<Pair> in;
        Set<Pair> out;

        InOut(final Set<Pair> in, final Set<Pair> out){
            this.in = in;
            this.out = out;
        }
    }

    /**
     * Holds Var and Usenode pairs: usenodes are needed to construct def-use chains
     */
    private static class Pair {
        String var;
        CFGNode node;

        Pair(final String var, final CFGNode node) {
            this.var = var;
            this.node = node;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof Pair)) return false;

            final Pair pair = (Pair) o;

            return var.equals(pair.var) && node.getNodeId() == pair.node.getNodeId();
        }

        private int hash = -1;

        @Override
        public int hashCode() {
            if (hash == -1) {
                hash = var.hashCode();
                hash = 31 * hash + node.hashCode();
            }
            return hash;
        }
    }
}
