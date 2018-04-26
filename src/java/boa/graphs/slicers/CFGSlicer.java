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

import boa.compiler.ast.statements.Statement;
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
 */

public class CFGSlicer {

    private ArrayList<CFGNode> slice = new ArrayList<CFGNode>();

    public CFGSlicer(Method m, CFGNode n) throws Exception {
        if (n != null) {
            List<CFGNode> entrynodes = new ArrayList<CFGNode>();
            entrynodes.add(n);
            getSlice(new CFG(m, true), entrynodes);
        }
    }

    public CFGSlicer(Method m, CFGNode[] n) throws Exception {
        List<CFGNode> entrynodes = new ArrayList<CFGNode>(Arrays.asList(n));
        getSlice(new CFG(m, true), entrynodes);
    }

    public CFGSlicer(Method m, int nid) throws Exception {
        List<CFGNode> entrynodes = new ArrayList<CFGNode>();
        CFG cfg = new CFG(m, true);
        CFGNode node = cfg.getNode(nid);
        if (node != null) {
            entrynodes.add(node);
            getSlice(cfg, entrynodes);
        }
    }

    public CFGSlicer(Method m, Integer[] nids) throws Exception {
        List<CFGNode> entrynodes = new ArrayList<CFGNode>();
        CFG cfg = new CFG(m, true);
        for (Integer i: nids) {
            CFGNode node = cfg.getNode(i);
            if (node != null)
                entrynodes.add(cfg.getNode(i));
        }
        if (entrynodes.size() > 0)
            getSlice(cfg, entrynodes);
    }
    // Getter
    private ArrayList<CFGNode> getSlice() {
        return slice;
    }

    /**
     * Compute slice of a method based on the given slicing variables
     *
     * @param cfg control flow graph
     * @param slicingNodes variable to be used for method slicing
     * @throws Exception
     */
    private void getSlice(final CFG cfg, final List<CFGNode> slicingNodes) throws Exception {

        final Set<CFGNode> inSlice = new HashSet<CFGNode>(slicingNodes);
        final Set<CFGNode> controlInflNodes = new HashSet<CFGNode>();
        final Map<Integer, Set<CFGNode>> infl = getInfluence(new PDTree(cfg), cfg);

        BoaAbstractTraversal slicer = new BoaAbstractTraversal<Set<String>>(true, true) {

            protected Set<String> preTraverse(final CFGNode node) throws Exception {
                Set<String> gen = new HashSet<String>();
                Set<String> kill = new HashSet<String>();
                Set<String> in = new HashSet<String>();
                Set<String> out = new HashSet<String>();

                // in(n) = \/(pred) out(pred)
                for (CFGNode p: node.getPredecessorsList()) {
                    Set<String> pred = getValue(p);
                    if (pred != null)
                        in.addAll(pred);
                }

                // gen(n) = def(n) and (ref(n) /\ in(n) != {} or inSlice(n))
                Set<String> refIn = new HashSet<String>(node.getUseVariables());
                refIn.retainAll(in);
                if (refIn.size() != 0 || inSlice.contains(node))
                    if (node.getDefVariables() != null)
                        gen.add(node.getDefVariables());

                // kill(n) = def(n)
                if (node.getDefVariables() != null)
                    kill.add(node.getDefVariables());

                // out(n) = gen(n) \/ (in(n) - kill(n))
                out.addAll(in);
                out.removeAll(kill);
                out.addAll(gen);

                // inSlice(n) if ref(n) /\ out(n) != {}
                Set<String> refOut = new HashSet<String>(node.getUseVariables());
                refOut.retainAll(out);
                if (refOut.size() != 0)
                    inSlice.add(node);

                // m -> infl(n)
                if (inSlice.contains(node) && node.getKind() == Control.CFGNode.CFGNodeType.CONTROL)
                    controlInflNodes.addAll(infl.get(node.getId()));

                return out;
            }

            @Override
            public void traverse(final CFGNode node, boolean flag) throws Exception {
                if(flag) {
                    currentResult = new HashSet<String>(preTraverse(node));
                    outputMapObj.put(node.getId(), new HashSet<String>(currentResult));
                }
                else
                    outputMapObj.put(node.getId(), new HashSet<String>(preTraverse(node)));
            }
        };

        BoaAbstractFixP fixp = new BoaAbstractFixP() {

            public boolean invoke1(final Set<String> current, final Set<String> previous) throws Exception {
                Set<String> curr = new HashSet<String>(current);
                curr.removeAll(previous);
                return curr.size() == 0;
            }

            @Override
            public boolean invoke(Object current, Object previous) throws Exception {
                return invoke1((HashSet<String>) current, (HashSet<String>) previous);
            }
        };

        slicer.traverse(cfg, TraversalDirection.FORWARD, TraversalKind.DFS, fixp);

        inSlice.addAll(controlInflNodes);
        slice.addAll(inSlice);
        Collections.sort(slice);
    }

    /**
     * Gives back the map of control nodes and dependent nodes.
     *
     * @param pdTree post dominator tree
     * @param cfg control flow graph
     * @return map of control nodes and dependent nodes
     */
    private Map<Integer, Set<CFGNode>> getInfluence(final PDTree pdTree, final CFG cfg) {
        Map<Integer[], String> controlEdges = new HashMap<Integer[], String>();
        for (CFGNode n : cfg.getNodes()) {
            if (n.getKind() == Control.CFGNode.CFGNodeType.CONTROL)
                for (CFGEdge e : n.outEdges)
                    if (e.label().equals("."))
                        controlEdges.put(new Integer[]{e.getSrc().getId(), e.getDest().getId()}, "F");
                    else
                        controlEdges.put(new Integer[]{e.getSrc().getId(), e.getDest().getId()}, e.label());
        }

        Map<Integer, Set<CFGNode>> contolDependentMap = new HashMap<Integer, Set<CFGNode>>();

        for (Integer[] enodes : controlEdges.keySet()) {
            TreeNode srcParent = pdTree.getNode(enodes[0]).getParent();
            TreeNode destination = pdTree.getNode(enodes[1]);
            if (!contolDependentMap.containsKey(enodes[0]))
                contolDependentMap.put(enodes[0],new HashSet<CFGNode>());
            while (!srcParent.equals(destination)) {
                contolDependentMap.get(enodes[0]).add(cfg.getNode(destination.getId()));
                destination = destination.getParent();
            }
        }

        return contolDependentMap;
    }

}
