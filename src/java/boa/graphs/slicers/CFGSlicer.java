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
package boa.graphs.slicers;

import boa.graphs.cfg.CFG;
import boa.graphs.cfg.CFGEdge;
import boa.graphs.cfg.CFGNode;
import boa.graphs.trees.PDTree;
import boa.graphs.trees.TreeNode;
import boa.runtime.BoaAbstractFixP;
import boa.runtime.BoaAbstractTraversal;
import boa.types.Ast.*;
import boa.types.Control;
import boa.types.Graph.Traversal.*;

import java.util.*;

/**
 * A forward slicer based on CFG
 *
 * @author marafat
 * @author rdyer
 */
public class CFGSlicer {
    private final ArrayList<CFGNode> slice = new ArrayList<CFGNode>();

    public CFGSlicer(final Method m, final CFGNode n) throws Exception {
        if (n != null) {
            final CFG cfg = new CFG(m, true).get();
            if (cfg == null) return;

            final List<CFGNode> entrynodes = new ArrayList<CFGNode>();
            entrynodes.add(n);
            getSlice(cfg, entrynodes);
        }
    }

    public CFGSlicer(final Method m, final CFGNode[] n) throws Exception {
        final CFG cfg = new CFG(m, true).get();
        if (cfg == null) return;

        final List<CFGNode> entrynodes = new ArrayList<CFGNode>(Arrays.asList(n));
        getSlice(cfg, entrynodes);
    }

    public CFGSlicer(final Method m, final int nid) throws Exception {
        final CFG cfg = new CFG(m, true).get();
        if (cfg == null) return;

        final List<CFGNode> entrynodes = new ArrayList<CFGNode>();
        final CFGNode node = cfg.getNode(nid);

        if (node != null) {
            entrynodes.add(node);
            getSlice(cfg, entrynodes);
        }
    }

    public CFGSlicer(final Method m, final Integer[] nids) throws Exception {
        final CFG cfg = new CFG(m, true).get();
        if (cfg == null) return;

        final List<CFGNode> entrynodes = new ArrayList<CFGNode>();
        for (final Integer i : nids) {
            final CFGNode node = cfg.getNode(i);
            if (node != null) {
                entrynodes.add(cfg.getNode(i));
            }
        }
        if (entrynodes.size() > 0) {
            getSlice(cfg, entrynodes);
        }
    }

    public ArrayList<CFGNode> getSlice() {
        return slice;
    }

    /**
     * Computes slice of the method based on the given slicing variables
     *
     * @param cfg control flow graph
     * @param slicingNodes variable to be used for method slicing
     * @throws Exception
     */
    private void getSlice(final CFG cfg, final List<CFGNode> slicingNodes) throws Exception {
        if (slicingNodes.size() == 0) return;

        final Set<CFGNode> inSlice = new HashSet<CFGNode>(slicingNodes);
        final Set<CFGNode> controlInflNodes = new HashSet<CFGNode>();
        final Map<Integer, Set<CFGNode>> infl = getInfluence(new PDTree(cfg), cfg);

        // TODO: get rid of the traversal
        final BoaAbstractTraversal slicer = new BoaAbstractTraversal<Set<String>>(true, true) {
            protected Set<String> preTraverse(final CFGNode node) throws Exception {
                // in(n) = \/(pred) out(pred)
                final Set<String> in = new HashSet<String>();
                for (final CFGNode p : node.getPredecessors()) {
                    final Set<String> pred = getValue(p);
                    if (pred != null)
                        in.addAll(pred);
                }

                // gen(n) = def(n) and (ref(n) /\ in(n) != {} or inSlice(n))
                String gen = null;
                final Set<String> refIn = new HashSet<String>(node.getUseVariables());
                refIn.retainAll(in);
                if (refIn.size() != 0 || inSlice.contains(node))
                    if (node.getDefVariables() != null)
                        gen = node.getDefVariables();

                // kill(n) = def(n)
                // use def directly

                // out(n) = gen(n) \/ (in(n) - kill(n))
                final Set<String> out = new HashSet<String>(in);
                if (node.getDefVariables() != null)
                    out.remove(node.getDefVariables());
                if (gen != null)
                    out.add(gen);

                // inSlice(n) if ref(n) /\ out(n) != {}
                final Set<String> refOut = new HashSet<String>(node.getUseVariables());
                refOut.retainAll(out);
                if (refOut.size() != 0)
                    inSlice.add(node);

                // m -> infl(n)
                if (inSlice.contains(node) && node.getKind() == Control.Node.NodeType.CONTROL)
                    controlInflNodes.addAll(infl.get(node.getNodeId()));

                return out;
            }

            @Override
            public void traverse(final CFGNode node, boolean flag) throws Exception {
                if (flag) {
                    currentResult = preTraverse(node);
                    outputMapObj.put(node.getId(), new HashSet<String>(currentResult));
                } else {
                    outputMapObj.put(node.getId(), preTraverse(node));
                }
            }
        };

        final BoaAbstractFixP fixp = new BoaAbstractFixP() {
            public boolean invoke1(final Set<String> current, final Set<String> previous) {
                final Set<String> curr = new HashSet<String>(current);
                curr.removeAll(previous);
                return curr.size() == 0;
            }

            @Override
            @SuppressWarnings({"unchecked"})
            public boolean invoke(final Object current, final Object previous) throws Exception {
                return invoke1((HashSet<String>) current, (HashSet<String>) previous);
            }
        };

        slicer.traverse(cfg, TraversalDirection.FORWARD, TraversalKind.DFS, fixp);

        inSlice.addAll(controlInflNodes);
        slice.addAll(inSlice);
        Collections.sort(slice);
    }

    /**
     * Returns the map of control nodes and dependent nodes.
     *
     * @param pdTree post dominator tree
     * @param cfg control flow graph
     * @return map of control nodes and dependent nodes
     */
    private Map<Integer, Set<CFGNode>> getInfluence(final PDTree pdTree, final CFG cfg) {
        // store source and desination of control edges with label
        final Map<Integer[], String> controlEdges = new HashMap<Integer[], String>();
        for (final CFGNode n : cfg.getNodes()) {
            if (n.getKind() == Control.Node.NodeType.CONTROL)
                for (final CFGEdge e : n.getOutEdges())
                    if (e.getLabel().equals("."))
                        controlEdges.put(new Integer[]{e.getSrc().getNodeId(), e.getDest().getNodeId()}, "F");
                    else
                        controlEdges.put(new Integer[]{e.getSrc().getNodeId(), e.getDest().getNodeId()}, e.getLabel());
        }

        // add the edge: entry ---> start
        final Map<Integer, Set<CFGNode>> contolDependentMap = new HashMap<Integer, Set<CFGNode>>();

        for (final Integer[] enodes : controlEdges.keySet()) {
            final TreeNode srcParent = pdTree.getNode(enodes[0]).getParent();
            TreeNode destination = pdTree.getNode(enodes[1]);
            if (!contolDependentMap.containsKey(enodes[0]))
                contolDependentMap.put(enodes[0], new HashSet<CFGNode>());
            while (!srcParent.equals(destination)) {
                contolDependentMap.get(enodes[0]).add(cfg.getNode(destination.getNodeId()));
                destination = destination.getParent();
            }
        }

        return contolDependentMap;
    }
}
