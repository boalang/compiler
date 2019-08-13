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

import java.util.*;

import boa.functions.BoaAstIntrinsics;
import boa.types.Ast.Method;
import boa.graphs.cfg.CFG;
import boa.graphs.cfg.CFGNode;

/**
 * Dominator Tree builder
 *
 * @author marafat
 * @author rdyer
 */
public class DTree {
    private Method md;
    private TreeNode rootNode;
    private final Set<TreeNode> nodes = new LinkedHashSet<TreeNode>();
    private boolean isEntryNode = false; // as specified in ferrante-1987 paper on PDG

    /**
     * Constructs a dominator tree
     *
     * @param cfg control flow graph
     * @throws Exception if tree construction fails
     */
    public DTree(final CFG cfg) throws Exception {
        if (cfg != null && cfg.getNodes().size() > 0) {
            this.md = cfg.getMd();
            final Map<CFGNode, Set<CFGNode>> dom = computeDominators(cfg);
            final Map<CFGNode, CFGNode> idom = computeImmediateDominator(dom, cfg);
            buildDomTree(idom);
        }
    }

    /**
     * Constructs a dominator tree
     *
     * @param md method whose dominator tree is to be built
     * @param paramAsStatement if true, inserts parameters as assign statements at the
     *                         begining of control flow graph. Default is set to false
     * @throws Exception if tree construction fails
     */
    public DTree(final Method md, boolean paramAsStatement) throws Exception {
        this(new CFG(md, paramAsStatement).get());
    }

    /**
     * Constructs a dominator tree
     *
     * @param md method whose dominator tree is to be built
     * @throws Exception if tree construction fails
     */
    public DTree(final Method md) throws Exception {
        this(new CFG(md).get());
    }

    /**
     * Augments tree with entry node
     */
    public void addEntryNode() {
        if (!isEntryNode) {
            final TreeNode entry = new TreeNode(nodes.size());
            entry.setParent(rootNode);
            rootNode.addChild(entry);
            nodes.add(entry);
            isEntryNode = true;
        }
    }

    /**
     * Returns the method whose dominator tree is built
     *
     * @return the method whose dominator tree is built
     */
    public Method getMethod() { return md; }

    /**
     * Returns the root node of the tree
     *
     * @return the root node of the tree
     */
    public TreeNode getRootNode() {
        return rootNode;
    }

    /**
     * Returns the set of all the nodes in the tree
     *
     * @return the set of all the nodes in the tree
     */
    public Set<TreeNode> getNodes() {
        return nodes;
    }

    /**
     * Returns the immediate dominator of the given node
     *
     * @param node whose immediate dominator is requested
     * @return the immediate dominator of the given node
     */
    public TreeNode getImmediateDominator(final TreeNode node) {
        for (final TreeNode n : nodes)
            if (n.equals(node))
                return n.getParent();
        return null;
    }

    /**
     * Returns the immediate dominator for the given node id
     *
     * @param nodeid of the node whose immediate dominator is requested
     * @return the immediate dominator for the given node id
     */
    public TreeNode getImmediateDominator(final int nodeid) {
        for (final TreeNode n : nodes)
            if (n.getNodeId() == nodeid)
                return n.getParent();
        return null;
    }

    /**
     * Returns the tree node for the given node id. If not found then returns null
     *
     * @param id node id
     * @return the tree node for the given node id. If not found then returns null
     */
    public TreeNode getNode(final int id) {
        for (final TreeNode node : nodes) {
            if (node.getNodeId() == id)
                return node;
        }
        return null;
    }

    /**
     * Computes and returns map of dominators for each node in the control flow graph
     *
     * @param cfg control flow graph
     * @return map of nodes and corresponding set of dominator nodes
     * @throws Exception
     */
    private Map<CFGNode, Set<CFGNode>> computeDominators(final CFG cfg) {
        final Map<CFGNode, Set<CFGNode>> pDomMap = new HashMap<CFGNode, Set<CFGNode>>();

        // initialize
        if (cfg.getNodes().size() > 2) {
            for (final CFGNode n : cfg.getNodes()) {
                if (n.getNodeId() == 0)
                    pDomMap.put(n, new LinkedHashSet<CFGNode>(Collections.singletonList(n)));
                else
                    pDomMap.put(n, cfg.getNodes());
            }
        } else {
            final CFGNode startNode = cfg.getNode(0);
            pDomMap.put(startNode, new LinkedHashSet<CFGNode>(Collections.singletonList(startNode)));
            pDomMap.put(cfg.getNode(1), new LinkedHashSet<CFGNode>(cfg.getNodes()));
        }

        final Map<CFGNode, Set<CFGNode>> currentPDomMap = new HashMap<CFGNode, Set<CFGNode>>();
        boolean saturated = false;
        while (!saturated) { // fix point iteration
            int changeCount = 0;
            for (final CFGNode n : cfg.getNodes()) {
                final Set<CFGNode> currentPDom = new LinkedHashSet<CFGNode>();

                // Intersection[pred(node)]
                boolean first = true;
                for (final CFGNode pred : n.getPredecessors()) {
                    if (first) {
                        currentPDom.addAll(pDomMap.get(pred));
                        first = false;
                        continue;
                    }
                    currentPDom.retainAll(pDomMap.get(pred));
                }
                // D[n] = {n} Union (Intersection[pred[node]])
                currentPDom.add(n);

                final Set<CFGNode> diff = new LinkedHashSet<CFGNode>(pDomMap.get(n));
                diff.removeAll(currentPDom);
                if (diff.size() > 0)
                    changeCount++;
                currentPDomMap.put(n, currentPDom);
            }

            if (changeCount == 0) {
                saturated = true;
            } else {
                pDomMap.clear();
                pDomMap.putAll(currentPDomMap);
                currentPDomMap.clear();
            }
        }

        // strict dominance
        for (final CFGNode n : pDomMap.keySet())
            pDomMap.get(n).remove(n);

        return pDomMap;
    }

    /**
     * Computes and returns map of nodes and corresponding immediate dominators
     *
     * @param dom map of nodes and corresponding dominators
     * @return map of nodes and corresponding immediate dominators
     */
    private Map<CFGNode, CFGNode> computeImmediateDominator(final Map<CFGNode, Set<CFGNode>> dom, final CFG cfg) {
        /*
         * To find idom, we check each dom of a node to see if it is dominating any other
         * node. Each node should have atmost one i-dom (first node has no immediate dominator)
         */
        final Map<CFGNode, CFGNode> idom = new HashMap<CFGNode, CFGNode>();
        for (final Map.Entry<CFGNode, Set<CFGNode>> entry : dom.entrySet()) {
            for (final CFGNode pd1 : entry.getValue()) {
                boolean isIPDom = true;
                for (final CFGNode pd2 : entry.getValue()) {
                    if (pd1.getNodeId() != pd2.getNodeId())
                        if ((dom.get(pd2)).contains(pd1)) {
                            isIPDom = false;
                            break;
                        }
                }
                if (isIPDom) {
                    idom.put(entry.getKey(), pd1);
                    break;
                }
            }
        }

        return idom;
    }

    /**
     * Builds a dominator tree using nodes and their immediate dominators
     *
     * @param idoms map of nodes and their immediate dominators
     */
    private void buildDomTree(final Map<CFGNode, CFGNode> idoms) throws Exception {
        /*
         * Create an edge between idom and corresponding node.
         * Since each node can have only one idom, the resulting graph will form a tree
         */
        try {
            for (final Map.Entry<CFGNode, CFGNode> entry : idoms.entrySet()) {
                final TreeNode src = getNode(entry.getValue());
                final TreeNode dest = getNode(entry.getKey());

                src.addChild(dest);
                dest.setParent(src);
            }

            rootNode = getNode(0);

        } catch (final Exception e) {
            System.out.println(BoaAstIntrinsics.prettyprint(md));
            throw e;
        }
    }

    /**
     * Returns the existing tree node for the given CFG node. If not found then returns a new node
     *
     * @param cfgNode control flow graph node
     * @return the existing tree node for the given CFG node. If not found then returns a new node
     */
    private TreeNode getNode(final CFGNode cfgNode) {
        final TreeNode node = getNode(cfgNode.getNodeId());
        if (node != null)
            return node;

        final TreeNode newNode = new TreeNode(cfgNode);
        nodes.add(newNode);
        return newNode;
    }
}
