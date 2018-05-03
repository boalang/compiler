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
import boa.runtime.BoaAbstractFixP;
import boa.runtime.BoaAbstractTraversal;
import boa.types.Graph.Traversal.*;

/**
 * Post Dominator Tree builder
 *
 * @author marafat
 */

public class PDTree {

    private Method md;
    private TreeNode rootNode;
    private HashSet<TreeNode> nodes = new HashSet<TreeNode>();

    public PDTree(final CFG cfg) throws Exception {
        this.md = cfg.md;
        if (cfg.getNodes().size() > 0) {
            Map<CFGNode, Set<CFGNode>> pdom = computePostDomonitors(cfg);
            Map<CFGNode, CFGNode> ipdom = computeImmediatePostDominator(pdom, cfg);
            buildPDomTree(ipdom, cfg.getNodes().size() - 1);
        }
    }

    public PDTree(final Method method, boolean paramAsStatement) throws Exception {
        this(new CFG(method, paramAsStatement));
    }

    public PDTree(final Method method) throws Exception {
        this(new CFG(method));
    }

    // Getters
    public Method getMethod() {
        return md;
    }

    public TreeNode getRootNode() {
        return rootNode;
    }

    public HashSet<TreeNode> getNodes() {
        return nodes;
    }

    /**
     * Returns the immediate post-dominator of the given node
     *
     * @param node whose immediate post-dominator is requested
     * @return parent TreeNode
     */
    public TreeNode getImmediatePostDominator(TreeNode node) {
        for (TreeNode n: nodes)
            if (n.equals(node))
                return n.getParent();
        return null;
    }

    /**
     * Returns the immediate post-dominator for the given node id
     *
     * @param nodeid of the node whose immediate post-dominator is requested
     * @return parent TreeNode
     */
    public TreeNode getImmediatePostDominator(int nodeid) {
        for (TreeNode n: nodes)
            if (n.getId() == nodeid)
                return n.getParent();
        return null;
    }

    /**
     * Gives back the node for the given node id, otherwise returns null
     *
     * @param id node id
     * @return tree nodes
     */
    public TreeNode getNode(int id) {
        for (TreeNode node: nodes) {
            if (node.getId() == id)
                return node;
        }
        return null;
    }

    /**
     * Computes post-dominators for each node in the control flow graph
     *
     * @param cfg control flow graph
     * @return map of node and corresponding set of post-dominator nodes
     * @throws Exception
     */
    private Map<CFGNode, Set<CFGNode>> computePostDomonitors(final CFG cfg) throws Exception {
        Map<CFGNode, Set<CFGNode>> pDomMap = new HashMap<CFGNode, Set<CFGNode>>();
        int stopid = cfg.getNodes().size()-1;
        for (CFGNode n: cfg.getNodes()) { // initialize
            if (n.getId() == stopid)
                pDomMap.put(n, new HashSet<CFGNode>(Collections.singletonList(n)));
            else
                pDomMap.put(n, cfg.getNodes());
        }

        boolean saturation = false;
        while (!saturation) { // iterate untill the fix point is reached
            Map<CFGNode, Set<CFGNode>> currentPDomMap = new HashMap<CFGNode, Set<CFGNode>>();
            int changeCount = 0;
            for (CFGNode n: cfg.getNodes()) {
                Set<CFGNode> currentPDom = new HashSet<CFGNode>();

                // Intersection[succ(node)]
                boolean first = true;
                for (CFGNode succ: n.getSuccessorsList()) {
                    if (first) {
                        currentPDom.addAll(pDomMap.get(succ));
                        first = false;
                        continue;
                    }
                    currentPDom.retainAll(pDomMap.get(succ));
                }
                // D[n] = {n} Union (Intersection[succ[node]])
                currentPDom.add(n);

                Set<CFGNode> diff = new HashSet<CFGNode>(pDomMap.get(n));
                diff.removeAll(currentPDom);
                if (diff.size() > 0)
                    changeCount++;
                currentPDomMap.put(n, currentPDom);
            }
            if (changeCount == 0)
                saturation = true;
            else
                pDomMap = currentPDomMap;
        }

        // strict post-dominators
        for (CFGNode n: pDomMap.keySet())
            pDomMap.get(n).remove(n);

        return pDomMap;
    }

    /**
     * Computes immediate post-dominator for each node
     *
     * @param pdom map of nodes and corresponding post-dominators
     * @return map of nodes and corresponding immediate post-dominator
     */
    private Map<CFGNode, CFGNode> computeImmediatePostDominator(final Map<CFGNode, Set<CFGNode>> pdom, final CFG cfg) {
        // inefficient implementation: t-complexity = O(n^3)
        /* To find ipdom, we check each pdom of a node to see if it is post dominating any other
         * node. Each node should have atmost one ip-dom (last node has no immediate post dominator)
         */
        Map<CFGNode, CFGNode> ipdom = new HashMap<CFGNode, CFGNode>();
        for (Map.Entry<CFGNode, Set<CFGNode>> entry : pdom.entrySet()) {
            for (CFGNode pd1 : entry.getValue()) {
                boolean isIPDom = true;
                for (CFGNode pd2 : entry.getValue()) {
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
     * @param stopid id of the stop node of the control graph
     */
    private void buildPDomTree(final Map<CFGNode, CFGNode> ipdoms, int stopid) {
        /* Create an edge between ipdom and corresponding node.
         * Since each node can have only one ipdom, the resulting graph will form a tree
         */
        try {
            for (Map.Entry<CFGNode, CFGNode> entry : ipdoms.entrySet()) {
                CFGNode ipdom = entry.getValue();

                TreeNode src = getNode(ipdom);
                TreeNode dest = getNode(entry.getKey());

                src.addChild(dest);
                dest.setParent(src);
            }

            rootNode = getNode(stopid);
            TreeNode entry = new TreeNode(stopid + 1);
            entry.setParent(rootNode);
            rootNode.addChild(entry);
            nodes.add(entry);

        } catch (Exception e) {
            System.out.println(BoaAstIntrinsics.prettyprint(md));
            throw e;
        }
    }

    /**
     * Checks if a node already exists and returns it, otherwise returns a new node.
     *
     * @param cfgNode a control flow graph node
     * @return a new tree node or an existing tree node
     */
    private TreeNode getNode(final CFGNode cfgNode) {
        TreeNode node = getNode(cfgNode.getId());
        if (node != null)
            return node;

        TreeNode newNode = new TreeNode(cfgNode);
        nodes.add(newNode);
        return newNode;
    }

}