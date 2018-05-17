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
 * Post Dominator Tree builder
 *
 * @author marafat
 * @author rdyer
 */
public class PDTree {
    private Method md;
    private TreeNode rootNode;
    private final HashSet<TreeNode> nodes = new HashSet<TreeNode>();
    private boolean isEntryNode = false; // as specified in ferrante-1987 paper on PDG

    /**
     * Constructs a post-dominator tree
     *
     * @param cfg control flow graph
     * @throws Exception if tree construction fails
     */
    public PDTree(final CFG cfg) throws Exception {
        this.md = cfg.md;
        if (cfg != null && cfg.getNodes().size() > 0) {
            final Map<CFGNode, Set<CFGNode>> pdom = computePostDomonitors(cfg);
            final Map<CFGNode, CFGNode> ipdom = computeImmediatePostDominator(pdom, cfg);
            buildPDomTree(ipdom);
        }
    }

    /**
     * Constructs a post-dominator tree
     *
     * @param md method whose post-dominator tree is to be built
     * @param paramAsStatement if true, inserts parameters as assign statements at the
     *                         begining of control flow graph. Default is set to false
     * @throws Exception if tree construction fails
     */
    public PDTree(final Method md, final boolean paramAsStatement) throws Exception {
        this(new CFG(md, paramAsStatement).get());
    }

    /**
     * Constructs a post-dominator tree
     *
     * @param md method whose post-dominator tree is to be built
     * @throws Exception if tree construction fails
     */
    public PDTree(final Method md) throws Exception {
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
     * Returns the method whose post-dominator tree is built
     *
     * @return the method whose post-dominator tree is built
     */
    public Method getMethod() {
        return md;
    }

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
    public HashSet<TreeNode> getNodes() {
        return nodes;
    }

    /**
     * Returns the immediate post-dominator of the given node
     *
     * @param node whose immediate post-dominator is requested
     * @return the immediate post-dominator of the given node
     */
    public TreeNode getImmediatePostDominator(final TreeNode node) {
        for (final TreeNode n : nodes)
            if (n.equals(node))
                return n.getParent();
        return null;
    }

    /**
     * Returns the immediate post-dominator for the given node id
     *
     * @param nodeid of the node whose immediate post-dominator is requested
     * @return the immediate post-dominator of the given node
     */
    public TreeNode getImmediatePostDominator(final int nodeid) {
        for (final TreeNode n : nodes)
            if (n.getId() == nodeid)
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
            if (node.getId() == id)
                return node;
        }
        return null;
    }

    /**
     * Computes and returns map of post-dominators for each node in the control flow graph
     *
     * @param cfg control flow graph
     * @return map of nodes and corresponding set of post-dominator nodes
     * @throws Exception
     */
    private Map<CFGNode, Set<CFGNode>> computePostDomonitors(final CFG cfg) {
        final Map<CFGNode, Set<CFGNode>> pDomMap = new HashMap<CFGNode, Set<CFGNode>>();
        final int stopid = cfg.getNodes().size() - 1;

        // initialize
        if (cfg.getNodes().size() > 2) {
            for (final CFGNode n : cfg.getNodes()) {
                if (n.getId() == stopid)
                    pDomMap.put(n, new HashSet<CFGNode>(Collections.singletonList(n)));
                else
                    pDomMap.put(n, cfg.getNodes());
            }
        } else {
            final CFGNode stopNode = cfg.getNode(1);
            pDomMap.put(cfg.getNode(0), new HashSet<CFGNode>(cfg.getNodes()));
            pDomMap.put(stopNode, new HashSet<CFGNode>(Collections.singletonList(stopNode)));
        }

        final Map<CFGNode, Set<CFGNode>> currentPDomMap = new HashMap<CFGNode, Set<CFGNode>>();
        boolean saturated = false;
        while (!saturated) { // fix point iteration
            int changeCount = 0;
            for (final CFGNode n : cfg.getNodes()) {
                final Set<CFGNode> currentPDom = new HashSet<CFGNode>();

                // Intersection[succ(node)]
                boolean first = true;
                for (final CFGNode succ: n.getSuccessorsList()) {
                    if (first) {
                        currentPDom.addAll(pDomMap.get(succ));
                        first = false;
                        continue;
                    }
                    currentPDom.retainAll(pDomMap.get(succ));
                }
                // D[n] = {n} Union (Intersection[succ[node]])
                currentPDom.add(n);

                final Set<CFGNode> diff = new HashSet<CFGNode>(pDomMap.get(n));
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

        // strict post-dominance
        for (final CFGNode n : pDomMap.keySet())
            pDomMap.get(n).remove(n);

        return pDomMap;
    }

    /**
     * Computes and returns a map of nodes and corresponding immediate post-dominators
     *
     * @param pdom map of nodes and corresponding post-dominators
     * @return map of nodes and corresponding immediate post-dominators
     */
    private Map<CFGNode, CFGNode> computeImmediatePostDominator(final Map<CFGNode, Set<CFGNode>> pdom, final CFG cfg) {
        /*
         * To find ipdom, we check each pdom of a node to see if it is post dominating any other
         * node. Each node should have atmost one ip-dom (last node has no immediate post dominator)
         */
        final Map<CFGNode, CFGNode> ipdom = new HashMap<CFGNode, CFGNode>();
        for (final Map.Entry<CFGNode, Set<CFGNode>> entry : pdom.entrySet()) {
            for (final CFGNode pd1 : entry.getValue()) {
                boolean isIPDom = true;
                for (final CFGNode pd2 : entry.getValue()) {
                    if (pd1.getId() != pd2.getId())
                        if ((pdom.get(pd2)).contains(pd1)) {
                            isIPDom = false;
                            break;
                        }
                }
                if (isIPDom) {
                    ipdom.put(entry.getKey(), pd1);
                    break;
                }
            }
        }

        return ipdom;
    }

    /**
     * Builds a post dominator tree using nodes and their immediate post-dominators
     *
     * @param ipdoms map of nodes and their immediate post-dominators
     */
    private void buildPDomTree(final Map<CFGNode, CFGNode> ipdoms) throws Exception {
        /*
         * Create an edge between ipdom and corresponding node.
         * Since each node can have only one ipdom, the resulting graph will form a tree
         */
        try {
            for (final Map.Entry<CFGNode, CFGNode> entry : ipdoms.entrySet()) {
                final TreeNode src = getNode(entry.getValue());
                final TreeNode dest = getNode(entry.getKey());

                src.addChild(dest);
                dest.setParent(src);
            }

            rootNode = getNode(nodes.size()-1);
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
        final TreeNode node = getNode(cfgNode.getId());
        if (node != null)
            return node;

        final TreeNode newNode = new TreeNode(cfgNode);
        nodes.add(newNode);
        return newNode;
    }
}
