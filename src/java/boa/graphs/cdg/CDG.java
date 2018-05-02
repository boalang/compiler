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
package boa.graphs.cdg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import boa.functions.BoaAstIntrinsics;
import boa.graphs.cfg.CFG;
import boa.graphs.cfg.CFGEdge;
import boa.graphs.cfg.CFGNode;
import boa.graphs.trees.PDTree;
import boa.graphs.trees.TreeNode;
import boa.types.Ast.Method;
import boa.types.Control;

/**
 * Control Dependence Graph builder
 *
 * @author marafat
 */

public class CDG {

    private Method md;
    private CDGNode entryNode;
    private HashSet<CDGNode> nodes = new HashSet<CDGNode>();

    public CDG(final CFG cfg) throws Exception {
        this.md = cfg.md;
        if (cfg.getNodes().size() > 0) {
            PDTree pdTree = new PDTree(cfg);
            constructCDG(pdTree, cfg);
        }
    }

    public CDG(final Method method, boolean paramAsStatement) throws Exception {
        this(new CFG(method, paramAsStatement));
    }

    public CDG(final Method method) throws Exception {
        this(new CFG(method));
    }

    // Getters
    public Method getMethod() {
        return md;
    }

    public CDGNode getEntryNode() {
        return entryNode;
    }

    public HashSet<CDGNode> getNodes() {
        return nodes;
    }

    /**
     * Gives back the node for the given node id, otherwise returns null
     *
     * @param id node id
     * @return CDGNode
     */
    public CDGNode getNode(int id) {
        for (CDGNode n: nodes)
            if (n.getId() == id)
                return n;

        return null;
    }

    /**
     * Builds a Control Dependence Graph using the post dominator tree and control edges from cfg
     *
     * @param pdTree post dominator tree
     * @param cfg control flow graph
     */
    private void constructCDG(final PDTree pdTree, final CFG cfg) {
        // store source and desination of control edges with label
        Map<Integer[], String> controlEdges = new HashMap<Integer[], String>();
        for (CFGNode n: cfg.getNodes()) {
            if (n.getKind() == Control.CFGNode.CFGNodeType.CONTROL)
                for (CFGEdge e: n.getOutEdges())
                    if (e.label().equals("."))
                        controlEdges.put(new Integer[]{e.getSrc().getId(), e.getDest().getId()}, "F");
                    else
                        controlEdges.put(new Integer[]{e.getSrc().getId(), e.getDest().getId()}, e.label());
        }
        // entry -> start
        controlEdges.put(new Integer[]{cfg.getNodes().size(), 0}, "T");

        try {
            for (Map.Entry<Integer[], String> entry : controlEdges.entrySet()) {
                CDGNode source = getNode(pdTree.getNode(entry.getKey()[0]));
                TreeNode srcParent = pdTree.getNode(entry.getKey()[0]).getParent();
                TreeNode dest = pdTree.getNode(entry.getKey()[1]);

                while (!srcParent.equals(dest)) {
                    CDGNode destination = getNode(dest);
                    source.addSuccessor(destination);
                    destination.addPredecessor(source);

                    CDGEdge edge = new CDGEdge(source, destination, entry.getValue());
                    source.addOutEdge(edge);
                    destination.addInEdge(edge);

                    dest = dest.getParent();
                }
            }

            // remove start node and replace it with entry
            CDGNode startNode = getNode(0);
            entryNode = getNode(cfg.getNodes().size()); // entryNode = exitid + 1
            CDGEdge startEdge = entryNode.getOutEdge(startNode);
            entryNode.getSuccessors().remove(startNode);
            entryNode.getOutEdges().remove(startEdge);
            nodes.remove(startNode);
            entryNode.setKind(Control.CDGNode.CDGNodeType.ENTRY);
            entryNode.setId(0);
        }
        catch (Exception e) {
            System.out.println(BoaAstIntrinsics.prettyprint(md));
        }
    }

    /**
     * Checks if a node already exists and returns it, otherwise returns a new node.
     *
     * @param treeNode a post dominator tree node
     * @return a new tree node or an existing tree node
     */
    private CDGNode getNode(final TreeNode treeNode) {
        CDGNode node = getNode(treeNode.getId());
        if (node != null)
            return node;

        CDGNode newNode = new CDGNode(treeNode);
        nodes.add(newNode);
        return newNode;
    }
}