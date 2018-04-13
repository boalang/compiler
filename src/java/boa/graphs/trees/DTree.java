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

import boa.types.Ast.Method;
import boa.graphs.cfg.CFG;
import boa.graphs.cfg.CFGNode;
import boa.runtime.BoaAbstractFixP;
import boa.runtime.BoaAbstractTraversal;
import boa.types.Graph.Traversal.*;

/**
 * @author marafat
 */

public class DTree {

    private TreeNode rootNode;
    private Set<TreeNode> nodes = new HashSet<TreeNode>();;

    public DTree(final CFG cfg) throws Exception {
        Map<CFGNode, Set<CFGNode>> dom = computeDominator(cfg);
        Map<CFGNode, CFGNode> idom = computeImmediateDominator(dom);
        buildDomTree(idom, cfg.getNodes().size());
    }

    public DTree(final Method method) throws Exception {
        this(new CFG(method));
    }

    //Getters
    public Set<TreeNode> getNodes() {
        return nodes;
    }

    public TreeNode getRootNode() {
        return rootNode;
    }

    /**
     * Gives the tree node if the id exists, null otherwise
     *
     * @param id
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
     * Computes dominators for each node in the control flow graph
     *
     * @param cfg control flow graph
     * @return map of node and corresponding set of dominator nodes
     * @throws Exception
     */
    private Map<CFGNode, Set<CFGNode>> computeDominator(final CFG cfg) throws Exception {
        final Set<CFGNode> nodeids = cfg.getNodes();

        final BoaAbstractTraversal dom = new BoaAbstractTraversal<Set<CFGNode>>(true, true) {

            protected Set<CFGNode> preTraverse(final CFGNode node) throws Exception {
                Set<CFGNode> currentDom = new HashSet<CFGNode>();

                if (node.getId() == 0)
                    currentDom = nodeids;

                if ((getValue(node) != null))
                    currentDom = getValue(node);

                for (CFGNode predecessor : node.getPredecessorsList()) {
                    if (predecessor != null) {
                        Set<CFGNode> predDom = getValue(predecessor);
                        if (predDom != null)
                            currentDom.retainAll(predDom);
                    }
                }

                if (node != null)
                    currentDom.add(node);

                return currentDom;
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

        dom.traverse(cfg, TraversalDirection.FORWARD, TraversalKind.REVERSEPOSTORDER, fixp);

        return dom.outputMapObj; //FIXME: fix the return type
    }

    /**
     * Computes immediate dominator for each node
     *
     * @param dom map of nodes and corresponding dominators
     * @return map of nodes and corresponding immediate dominator
     */
    private Map<CFGNode, CFGNode> computeImmediateDominator(final Map<CFGNode, Set<CFGNode>> dom) {
        //Inefficient implementation: t-complexity = O(n^3)
        //To find idom, we check each dom of a node to see if it is dominating any other
        //node. Each node should have atmost one i-dom (first node has no immediate dominator)
        Map<CFGNode, CFGNode> idom = new HashMap<CFGNode, CFGNode>();
        for (CFGNode n : dom.keySet()) {
            for (CFGNode pd1 : dom.get(n)) {
                boolean isIPDom = true;
                for (CFGNode pd2 : dom.get(n)) {
                    if (!pd1.equals(pd2))
                        if ((dom.get(pd2)).contains(pd1)) {
                            isIPDom = false;
                            break;
                        }
                }
                if (isIPDom) {
                    idom.put(n, pd1);
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
        //Create an edge between idom and corresponding node.
        //Since each node can have only one idom, the resulting graph will form a tree
        for (CFGNode n : idoms.keySet()) {
            CFGNode idom = idoms.get(n);

            TreeNode src = getNode(idom);
            TreeNode dest = getNode(n);

            src.addChild(dest);
            dest.setParent(src);

            if (src.getId() == 0)
                rootNode = src;
        }

        TreeNode entry = new TreeNode(stopid+1);
        entry.setParent(rootNode);
        rootNode.addChild(entry);
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
                if (n == node)
                    return n;
            }
        }
        node.setStmt(cfgNode.getStmt());
        node.setExpr(cfgNode.getExpr());
        nodes.add(node);

        return node;
    }

}