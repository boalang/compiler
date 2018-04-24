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

    private List<CFGNode> slice = new ArrayList<CFGNode>();

    public CFGSlicer(Method m, String[] slicingVar) throws Exception {
        getSlice(new CFG(m), Arrays.asList(slicingVar));
    }

    public CFGSlicer(Method m, Variable[] slicingVar) throws Exception {
        List<String> sliceVar = new ArrayList<String>();
        for (Variable v: slicingVar)
            sliceVar.add(v.getName());
        getSlice(new CFG(m), sliceVar);
    }

    public CFGSlicer(Method m) throws Exception {
        List<String> sliceVar = new ArrayList<String>();
        for (Variable v: m.getArgumentsList())
            sliceVar.add(v.getName());
        getSlice(new CFG(m), sliceVar);
    }

    // Getter
    private List<CFGNode> getSlice() {
        return slice;
    }

    /**
     * Compute slice of a method based on the given slicing variables
     *
     * @param cfg control flow graph
     * @param slicingVar variable to be used for method slicing
     * @throws Exception
     */
    private void getSlice(final CFG cfg, final List<String> slicingVar) throws Exception {

        final Set<CFGNode> slicedNodes = new HashSet<CFGNode>();
        final Set<CFGNode> controlInflNodes = new HashSet<CFGNode>();
        final Map<Integer, Set<CFGNode>> infl = getInfluence(new PDTree(cfg), cfg);

        BoaAbstractTraversal slicer = new BoaAbstractTraversal<Set<String>>(true, true) {

            protected void preTraverse(final CFGNode node) throws Exception {
                Set<String> relevantVar;

                if (node.getId() == 0) {
                    relevantVar = new HashSet<String>(slicingVar);
                } else {
                    relevantVar = new HashSet<String>(outputMapObj.get(node.getId()));
                }

                for (CFGNode succ: node.getSuccessorsList()) {
                    Set<String> ref = new HashSet<String>(succ.getUseVariables());
                    ref.retainAll(relevantVar);

                    if (!outputMapObj.containsKey(succ.getId()))
                        outputMapObj.put(succ.getId(), new HashSet<String>());

                    if (ref.size() != 0) {
                        if (!succ.getDefVariables().equals("")) {
                            outputMapObj.get(succ.getId()).add(succ.getDefVariables());
                            Set<String> diff = new HashSet<String>(relevantVar);
                            diff.removeAll(succ.getUseVariables());
                            outputMapObj.get(succ.getId()).addAll(diff);
                        }
                        else
                            outputMapObj.get(succ.getId()).addAll(relevantVar);
                        slicedNodes.add(succ);
                    }
                    else
                        outputMapObj.get(succ.getId()).addAll(relevantVar);

                    if (slicedNodes.contains(succ) && succ.getKind() == Control.CFGNode.CFGNodeType.CONTROL)
                        controlInflNodes.addAll(infl.get(succ.getId()));
                }

            }

            @Override
            public void traverse(final CFGNode node, boolean flag) throws Exception {
                preTraverse(node);
            }
        };

        BoaAbstractFixP fixp = new BoaAbstractFixP() {
            boolean invoke1(final Set<String> current, final Set<String> previous) throws Exception {
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

        slicedNodes.addAll(controlInflNodes);
        slice.addAll(slicedNodes);
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
