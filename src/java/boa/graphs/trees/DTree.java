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
 * Dominator Tree builder
 *
 * @author marafat
 */

public class DTree {

    private Method md;
    private TreeNode rootNode;
    private HashSet<TreeNode> nodes = new HashSet<TreeNode>();

    public DTree(final CFG cfg) throws Exception {
        this.md = cfg.md;
        if (cfg.getNodes().size() > 0) {
            Map<CFGNode, Set<CFGNode>> dom = computeDominators(cfg);
            Map<CFGNode, CFGNode> idom = computeImmediateDominator(dom, cfg);
            buildDomTree(idom, cfg.getNodes().size());
        }
    }

    public DTree(final Method method, boolean paramAsStatement) throws Exception {
        this(new CFG(method, paramAsStatement));
    }

    public DTree(final Method method) throws Exception {
        this(new CFG(method));
    }

    // Getters
    public Method getMethod() { return md; }

    public TreeNode getRootNode() {
        return rootNode;
    }

    public HashSet<TreeNode> getNodes() {
        return nodes;
    }

    /**
     * Returns the immediate dominator of the given node
     *
     * @param node whose immediate dominator is requested
     * @return parent TreeNode
     */
    public TreeNode getImmediateDominator(TreeNode node) {
        for (TreeNode n: nodes)
            if (n.equals(node))
                return n.getParent();
        return null;
    }

    /**
     * Returns the immediate dominator for the given node id
     *
     * @param nodeid of the node whose immediate dominator is requested
     * @return parent TreeNode
     */
    public TreeNode getImmediateDominator(int nodeid) {
        for (TreeNode n: nodes)
            if (n.getId() == nodeid)
                return n.getParent();
        return null;
    }

    /**
     * Gives back the node for the given node id, otherwise returns null
     *
     * @param id node id
     * @return tree node
     */
    public TreeNode getNode(int id) {
        for (TreeNode node: nodes) {
            if (node.getId() == id)
                return node;
        }
        return null;
    }

    /**
     * Computes dominators for each node in the control flow graph
     *
     * @param cfg control flow graph
     * @return map of node and corresponding set of dominator nodes
     * @throws Exception
     */
    private Map<CFGNode, Set<CFGNode>> computeDominators(final CFG cfg) throws Exception {
        Map<CFGNode, Set<CFGNode>> pDomMap = new HashMap<CFGNode, Set<CFGNode>>();
        for (CFGNode n: cfg.getNodes()) { // initialize
            if (n.getId() == 0)
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

                // Intersection[pred(node)]
                boolean first = true;
                for (CFGNode pred: n.getPredecessorsList()) {
                    if (first) {
                        currentPDom.addAll(pDomMap.get(pred));
                        first = false;
                        continue;
                    }
                    currentPDom.retainAll(pDomMap.get(pred));
                }
                // D[n] = {n} Union (Intersection[pred[node]])
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

        // strict dominators
        for (CFGNode n: pDomMap.keySet())
            pDomMap.get(n).remove(n);

        return pDomMap;
    }

    /**
     * Computes immediate dominator for each node
     *
     * @param dom map of nodes and corresponding dominators
     * @return map of nodes and corresponding immediate dominator
     */
    private Map<CFGNode, CFGNode> computeImmediateDominator(final Map<CFGNode, Set<CFGNode>> dom, final CFG cfg) {
        // inefficient implementation: t-complexity = O(n^3)
        /* To find idom, we check each dom of a node to see if it is dominating any other
         * node. Each node should have atmost one i-dom (first node has no immediate dominator)
         */
        Map<CFGNode, CFGNode> idom = new HashMap<CFGNode, CFGNode>();
        for (Map.Entry<CFGNode, Set<CFGNode>> entry : dom.entrySet()) {
            for (CFGNode pd1 : entry.getValue()) {
                boolean isIPDom = true;
                for (CFGNode pd2 : entry.getValue()) {
                    if (pd1.getId() != pd2.getId())
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
     * @param stopid id of the stop node of the control graph
     */
    private void buildDomTree(final Map<CFGNode, CFGNode> idoms, int stopid) {
        /* Create an edge between idom and corresponding node.
         * Since each node can have only one idom, the resulting graph will form a tree
         */
        try {
            for (Map.Entry<CFGNode, CFGNode> entry : idoms.entrySet()) {
                CFGNode idom = entry.getValue();

                TreeNode src = getNode(idom);
                TreeNode dest = getNode(entry.getKey());

                src.addChild(dest);
                dest.setParent(src);
            }

            rootNode = getNode(0);
            TreeNode entry = new TreeNode(stopid + 1);
            entry.setParent(rootNode);
            rootNode.addChild(entry);
            nodes.add(entry);

        } catch (Exception e) {
            System.out.println(BoaAstIntrinsics.prettyprint(md));
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