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
import java.util.Set;

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
 * @author rdyer
 */
public class CDG {
    private Method md;
    private CDGNode entryNode;
    private final Set<CDGNode> nodes = new HashSet<CDGNode>();

    /**
     * Constructs a control dependence graph
     *
     * @param cfg control flow graph
     * @throws Exception if CDG construction fails
     */
    public CDG(final CFG cfg) throws Exception {
        if (cfg != null && cfg.getNodes().size() > 0) {
            this.md = cfg.getMd();
            final PDTree pdTree = new PDTree(cfg);
            pdTree.addEntryNode();
            constructCDG(pdTree, cfg);
        }
    }

    /**
     * Constructs a control dependence graph
     *
     * @param md method whose CDG is to be built
     * @param paramAsStatement if true, inserts parameters as assign statements at the
     *                         begining of control flow graph. Default is set to false
     * @throws Exception if CDG construction fails
     */
    public CDG(final Method md, boolean paramAsStatement) throws Exception {
        this(new CFG(md, paramAsStatement).get());
    }

    /**
     * Constructs a control dependence graph
     *
     * @param md method whose CDG is to be built
     * @throws Exception if CDG construction fails
     */
    public CDG(final Method md) throws Exception {
        this(md, false);
    }

    /**
     * Returns the method whose CDG is built
     *
     * @return the method whose CDG is built
     */
    public Method getMethod() {
        return md;
    }

    /**
     * Returns the entry node to the graph
     *
     * @return the entry node to the graph
     */
    public CDGNode getEntryNode() {
        return entryNode;
    }

    /**
     * Returns the set of all the nodes in the graph
     *
     * @return the set of all the nodes in the graph
     */
    public Set<CDGNode> getNodes() {
        return nodes;
    }

	public CDGNode[] sortNodes() {
		try {
			final CDGNode[] results = new CDGNode[nodes.size()];
			for (final CDGNode node : nodes) {
				results[node.getId()] = node;
			}
			return results;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

    /**
     * Returns the CDG node for the given node id. If not found then returns null
     *
     * @param id node id
     * @return the CDG node for the given node id. If not found then returns null
     */
    public CDGNode getNode(final int id) { // sort and do b-search
        for (final CDGNode n : nodes)
            if (n.getId() == id)
                return n;

        return null;
    }

    /**
     * Builds a control dependence graph using the post dominator tree and control edges from CFG
     *
     * @param pdTree post dominator tree
     * @param cfg control flow graph
     */
    private void constructCDG(final PDTree pdTree, final CFG cfg) throws Exception {
        // store source and desination of control edges with label
        final Map<Integer[], String> controlEdges = new HashMap<Integer[], String>();
        for (final CFGNode n : cfg.sortNodes()) {
            if (n.getKind() == Control.Node.NodeType.CONTROL)
                for (final CFGEdge e : n.getOutEdges())
                    if (e.getLabel().equals("."))
                        controlEdges.put(new Integer[]{e.getSrc().getId(), e.getDest().getId()}, "F");
                    else
                        controlEdges.put(new Integer[]{e.getSrc().getId(), e.getDest().getId()}, e.getLabel());
        }

        // add the edge: entry ---> start
        controlEdges.put(new Integer[]{cfg.getNodes().size(), 0}, "T");

        // for the given edge A ---> B, traverse from node B to the parent of node A
        try {
            for (final Map.Entry<Integer[], String> entry : controlEdges.entrySet()) {
                final TreeNode srcTreeNode = pdTree.getNode(entry.getKey()[0]);
                final TreeNode srcParent = srcTreeNode.getParent();
                final CDGNode source = getNode(srcTreeNode);
                TreeNode dest = pdTree.getNode(entry.getKey()[1]);

                while (!srcParent.equals(dest)) {
                    new CDGEdge(source, getNode(dest), entry.getValue());
                    dest = dest.getParent();
                }
            }

            // remove start node and replace it with entry
            final CDGNode startNode = getNode(0);
            entryNode = getNode(cfg.getNodes().size()); // entryNode = exitid + 1

            startNode.delete();
            nodes.remove(startNode);

            entryNode.setKind(Control.Node.NodeType.ENTRY);
            entryNode.setId(0);
        } catch (final Exception e) {
            System.out.println(BoaAstIntrinsics.prettyprint(md));
            throw e;
        }
    }

    /**
     * Returns the existing CDG node for the given Tree node. If not found then returns a new node
     *
     * @param treeNode post dominator tree node
     * @return the existing CDG node for the given Tree node. If not found then returns a new node
     */
    private CDGNode getNode(final TreeNode treeNode) throws Exception {
        try {
            final CDGNode node = getNode(treeNode.getId());
            if (node != null)
                return node;

            final CDGNode newNode = new CDGNode(treeNode);
            nodes.add(newNode);
            return newNode;
        } catch (final Exception e) {
            System.out.println(BoaAstIntrinsics.prettyprint(md));
            throw e;
        }
    }
}
