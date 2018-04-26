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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

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
    private HashSet<TreeNode> nodes = new HashSet<TreeNode>();;

    public PDTree(final CFG cfg) throws Exception {
        this.md = cfg.md;
        if (cfg.getNodes().size() > 0) {
            Map<Integer, Set<CFGNode>> pdom = computePostDominator(cfg);
            Map<CFGNode, CFGNode> ipdom = computeImmediatePostDominator(pdom, cfg);
            buildPDomTree(ipdom, cfg.getNodes().size() - 1);
        }
    }

    public PDTree(final Method method, boolean paramAsStatement) throws Exception {
        this(new CFG(method, paramAsStatement));
    }

    public PDTree(final Method method) throws Exception {
        this(new CFG(method, false));
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
     * Gives back the tree node if the id exists, null otherwise
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
    private Map<Integer, Set<CFGNode>> computePostDominator(final CFG cfg) throws Exception {
        final Set<CFGNode> cfgnodes = cfg.getNodes();
        final int stopid = cfgnodes.size() - 1;

        final BoaAbstractTraversal pdom = new BoaAbstractTraversal<Set<CFGNode>>(true, true) {

            protected Set<CFGNode> preTraverse(final CFGNode node) throws Exception {
                Set<CFGNode> currentPDom = new HashSet<CFGNode>();

                if (node.getId() != (stopid))
                    currentPDom = new HashSet<CFGNode>(cfgnodes);

                if ((getValue(node) != null))
                    currentPDom = getValue(node);

                for (CFGNode successor : node.getSuccessorsList()) {
                    if (successor != null) {
                        Set<CFGNode> succPDom = getValue(successor);
                        if (succPDom != null)
                            currentPDom.retainAll(succPDom);
                    }
                }

                currentPDom.add(node);

                return currentPDom;
            }

            @Override
            public void traverse(final CFGNode node, boolean flag) throws Exception {
                if (flag) {
                    currentResult = preTraverse(node);
                    outputMapObj.put(node.getId(), currentResult);
                } else
                    outputMapObj.put(node.getId(), preTraverse(node));
            }

        };

        BoaAbstractFixP fixp = new BoaAbstractFixP() {
            boolean invoke1(final Set<CFGNode> current, final Set<CFGNode> previous) throws Exception {
                Set<CFGNode> curr = new HashSet<CFGNode>(current);
                curr.removeAll(previous);
                return curr.size() == 0;
            }

            @Override
            public boolean invoke(Object current, Object previous) throws Exception {
                return invoke1((HashSet<CFGNode>) current, (HashSet<CFGNode>) previous);
            }
        };

        pdom.traverse(cfg, TraversalDirection.BACKWARD, TraversalKind.DFS, fixp);

        // remove self node id from each p-dom set because no node can post-dominate itself
        for (int nid: (Set<Integer>) pdom.outputMapObj.keySet()) {
            CFGNode node = cfg.getNode(nid);
            ((Map<Integer, Set<Integer>>)pdom.outputMapObj).get(nid).remove(node);
        }

        return pdom.outputMapObj;
    }

    /**
     * Computes immediate post-dominator for each node
     *
     * @param pdom map of nodes and corresponding post-dominators
     * @return map of nodes and corresponding immediate post-dominator
     */
    private Map<CFGNode, CFGNode> computeImmediatePostDominator(final Map<Integer, Set<CFGNode>> pdom, final CFG cfg) {
        // inefficient implementation: t-complexity = O(n^3)
        /* To find ipdom, we check each pdom of a node to see if it is post dominating any other
         * node. Each node should have atmost one ip-dom (last node has no immediate post dominator)
         */
        Map<CFGNode, CFGNode> ipdom = new HashMap<CFGNode, CFGNode>();
        for (Integer nid : pdom.keySet()) {
            for (CFGNode pd1 : pdom.get(nid)) {
                boolean isIPDom = true;
                for (CFGNode pd2 : pdom.get(nid)) {
                    if (pd1.getId() != pd2.getId())
                        if ((pdom.get(pd2.getId())).contains(pd1)) {
                            isIPDom = false;
                            break;
                        }
                }
                if (isIPDom) {
                    ipdom.put(cfg.getNode(nid), pd1);
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
        for (CFGNode n : ipdoms.keySet()) {
            CFGNode ipdom = ipdoms.get(n);

            TreeNode src = getNode(ipdom);
            TreeNode dest = getNode(n);

            src.addChild(dest);
            dest.setParent(src);
        }
        try {
            rootNode = getNode(stopid);
            TreeNode entry = new TreeNode(stopid + 1);
            entry.setParent(rootNode);
            rootNode.addChild(entry);
            nodes.add(entry);
        }
        catch (Exception e) {
            System.out.println("PDTree1 " + stopid + " : " + nodes.size());
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
        TreeNode node = new TreeNode(cfgNode.getId());
        if (nodes.contains(node)) {
            for (TreeNode n : nodes) {
                if (n.equals(node))
                    return n;
            }
        }
        node.setStmt(cfgNode.getStmt());
        node.setExpr(cfgNode.getExpr());
        node.setKind(cfgNode.getKind());
        node.setDefVariable(cfgNode.getDefVariables());
        node.setUseVariables(cfgNode.getUseVariables());
        nodes.add(node);

        return node;
    }

}