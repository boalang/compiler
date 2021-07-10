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
    private boolean hasEntryNode = false; // as specified in ferrante-1987 paper on PDG

    /**
     * Constructs a post-dominator tree
     *
     * @param cfg control flow graph
     * @throws Exception if tree construction fails
     */
    public PDTree(final CFG cfg) throws Exception {
        if (cfg != null && cfg.getNodes().size() > 0) {
            this.md = cfg.getMd();
            final Map<CFGNode, BitSet> pdom = computePostDomonitors(cfg);
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
        if (!hasEntryNode) {
            final TreeNode entry = new TreeNode(nodes.size());
            entry.setParent(rootNode);
            rootNode.addChild(entry);
            nodes.add(entry);
            hasEntryNode = true;
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

	public TreeNode[] sortNodes() {
		try {
			final TreeNode[] results = new TreeNode[nodes.size()];
			for (final TreeNode node : nodes) {
				results[node.getNodeId()] = node;
			}
			return results;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
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
     * Computes and returns map of post-dominators for each node in the control flow graph
     *
     * @param cfg control flow graph
     * @return map of nodes and corresponding set of post-dominator nodes
     */
    private Map<CFGNode, BitSet> computePostDomonitors(final CFG cfg) {
        final CFGNode[] cfgNodes = cfg.reverseSortNodes();
        final Map<CFGNode, List<CFGNode>> successors = new HashMap<CFGNode, List<CFGNode>>();

        Map<CFGNode, BitSet> pDomMap = new HashMap<CFGNode, BitSet>();
        Map<CFGNode, BitSet> currentPDomMap = new HashMap<CFGNode, BitSet>();

        // initialize
        final BitSet allBits = new BitSet();
        for (final CFGNode n : cfg.getNodes()) {
            allBits.set(n.getNodeId());
        }

        if (cfg.getNodes().size() > 2) {
            final int stopid = cfg.getNodes().size() - 1;

            for (final CFGNode n : cfg.getNodes()) {
                if (n.getNodeId() == stopid) {
                    pDomMap.put(n, new BitSet());
                    pDomMap.get(n).set(n.getNodeId());
                } else {
                    pDomMap.put(n, (BitSet)allBits.clone());
                }

                successors.put(n, n.getSuccessors());
            }
        } else {
            pDomMap.put(cfg.getNode(0), (BitSet)allBits.clone());

            final CFGNode stopNode = cfg.getNode(1);
            pDomMap.put(stopNode, new BitSet());
            pDomMap.get(stopNode).set(stopNode.getNodeId());

            successors.put(cfg.getNode(0), cfg.getNode(0).getSuccessors());
            successors.put(stopNode, stopNode.getSuccessors());
        }

        while (true) { // fix point iteration
            boolean changed = false;

            for (final CFGNode n : cfgNodes) {
                BitSet currentPDom = null;

                // Intersection[succ(node)]
                for (final CFGNode succ : successors.get(n)) {
                    if (currentPDom == null) {
                        currentPDom = (BitSet)pDomMap.get(succ).clone();
                        continue;
                    }
                    currentPDom.and(pDomMap.get(succ));
                    if (currentPDom.isEmpty())
                        break;
                }
                // D[n] = {n} Union (Intersection[succ[node]])
                if (currentPDom == null) currentPDom = new BitSet();
                currentPDom.set(n.getNodeId());

                if (!pDomMap.get(n).equals(currentPDom))
                    changed = true;
                currentPDomMap.put(n, currentPDom);
            }

            if (!changed)
                break;

            pDomMap = currentPDomMap;
            currentPDomMap = new HashMap<CFGNode, BitSet>();
        }

        // ensure strict post-dominance
        for (final CFGNode n : cfgNodes) {
            final BitSet curMap = pDomMap.get(n);
            curMap.clear(n.getNodeId());
        }

        return pDomMap;
    }

    /**
     * Computes and returns a map of nodes and corresponding immediate post-dominators
     *
     * @param pdom map of nodes and corresponding post-dominators
     * @return map of nodes and corresponding immediate post-dominators
     */
    private Map<CFGNode, CFGNode> computeImmediatePostDominator(final Map<CFGNode, BitSet> pdom, final CFG cfg) {
        final Map<CFGNode, CFGNode> ipdom = new HashMap<CFGNode, CFGNode>();

        // To find ipdom, we check each pdom of a node to see if it is post dominating any other
        // node. Each node should have at most one ip-dom (last node has no immediate post dominator)
        for (final Map.Entry<CFGNode, BitSet> entry : pdom.entrySet()) {
            BitSet bs1 = entry.getValue();
            for (int pd1 = 0; pd1 < bs1.size(); pd1++) {
                if (!bs1.get(pd1)) continue;
                boolean isIPDom = true;
                for (int pd2 = 0; pd2 < bs1.size(); pd2++) {
                    if (!bs1.get(pd2) || pd1 == pd2) continue;
                    if (pdom.containsKey(cfg.getNode(pd2)) && pdom.get(cfg.getNode(pd2)).get(pd1)) {
                        isIPDom = false;
                        break;
                    }
                }
                if (isIPDom) {
                    ipdom.put(entry.getKey(), cfg.getNode(pd1));
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
        final TreeNode node = getNode(cfgNode.getNodeId());
        if (node != null)
            return node;

        final TreeNode newNode = new TreeNode(cfgNode);
        nodes.add(newNode);
        return newNode;
    }
}
