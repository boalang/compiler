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
package boa.graphs.ddg;

import boa.functions.BoaIntrinsics;
import boa.graphs.cfg.CFG;
import boa.graphs.cfg.CFGNode;
import boa.runtime.BoaAbstractFixP;
import boa.runtime.BoaAbstractTraversal;
import boa.types.Graph.Traversal;
import boa.types.Ast.*;

import java.util.*;

/**
 * @author marafat
 */

public class DDG {

    private DDGNode entryNode;
    private Map<DDGNode, Set<DDGNode>> defUseChain;
    private Map<DDGNode, Set<DDGNode>> useDefChain; //TODO: needs reaching-def analysis

    public DDG(final CFG cfg) throws Exception {
        Map<CFGNode, InOut> liveVars = getLiveVariables(cfg);
        formDefUseChains(liveVars);
    }

    public DDG(final Method m) throws Exception {
        this(new CFG());
    }

    //Getters
    public Map<DDGNode, Set<DDGNode>> getDefUseChain() {
        return defUseChain;
    }

    public Set<DDGNode> getUseNodes(final DDGNode node) {
        return defUseChain.get(node);
    }

    public Set<DDGNode> getDefNodes(final String var) {
        Set<DDGNode> defNodes = new HashSet<DDGNode>();
        for (DDGNode n: defUseChain.keySet()) {
            if (n.getDefVariables().equals(var))
                defNodes.add(n);
        }

        return defNodes;
    }

    /**
     * computes in and out variables for each node
     *
     * @param cfg control flow graph
     * @return map of nodes and in, out variables
     * @throws Exception
     */
    private HashMap<CFGNode, InOut> getLiveVariables(final CFG cfg) throws Exception {
        BoaAbstractTraversal liveVar = new BoaAbstractTraversal<InOut>(true, true) {
            
            protected InOut preTraverse(final CFGNode node) throws Exception {
                InOut currentNode = new InOut();

                if ((getValue(node) != null)) {
                    currentNode = getValue(node);
                }

                for (CFGNode s: node.getSuccessorsList()) {
                    InOut succ = getValue(s);
                    if (succ != null)
                        currentNode.out.addAll(succ.in);
                }

                if (node.defVariables != null) {
                    currentNode.out.remove(new Pair(node.defVariables, node));
                    Set<Pair> useVars = new HashSet<Pair>();
                    for (String var: node.useVariables)
                        useVars.add(new Pair(var, node));
                    currentNode.in = BoaIntrinsics.set_union(useVars, currentNode.out);
                }

                return currentNode;
            }

            @Override
            public void traverse(final CFGNode node, boolean flag) throws Exception {
                if(flag) {
                    currentResult = new InOut(preTraverse(node));
                    outputMapObj.put(node.getId(), new InOut(currentResult));
                }
                else
                    outputMapObj.put(node.getId(), new InOut(preTraverse(node)));
            }
        };

        BoaAbstractFixP fixp = new boa.runtime.BoaAbstractFixP() {

            public boolean invoke1(final InOut curr, final InOut prev) throws Exception {
                return BoaIntrinsics.set_difference(curr.in, prev.in).size() == 0;
            }

            @Override
            public boolean invoke(Object curr, Object prev) throws Exception{
                return invoke1((InOut) curr, (InOut) prev);
            }
        };

        liveVar.traverse(cfg , Traversal.TraversalDirection.BACKWARD, Traversal.TraversalKind.POSTORDER, fixp);

        return liveVar.outputMapObj; //FIXME: fix the return type
    }

    /**
     * Constructs def-use chains for data flow between variables
     *
     * @param liveVar map of nodes and their in and out variables
     */
    private void formDefUseChains(final Map<CFGNode, InOut> liveVar) {
        //match def variable of the node with the out variable. If the match occurs form a def-use mapping
        for (CFGNode n: liveVar.keySet()) {
            for (Pair p: liveVar.get(n).out) {
                if (n.defVariables != null) {
                    if (n.defVariables.equals(p.var)) {
                        DDGNode defNode = new DDGNode(n);
                        DDGNode useNode = new DDGNode(p.node);
                        if (!defUseChain.containsKey(defNode))
                            defUseChain.put(defNode, new HashSet<DDGNode>());
                        defUseChain.get(defNode).add(useNode);
                    }
                }
            }
        }
    }

    //Holds in and out pairs for each node
    private class InOut {
        Set<Pair> in;
        Set<Pair> out;

        InOut() {
            in = new HashSet<Pair>();
            out = new HashSet<Pair>();
        }

        InOut(final HashSet<Pair> out, final HashSet<Pair> in){
            this.out = out;
            this.in = in;
        }

        InOut(final InOut tmp){
            this.out = tmp.out;
            this.in = tmp.in;
        }

        public InOut clone() {
            return new InOut(this);
        }
    }

    //(var, useNode) pair: use nodes are needed to construct def-use chains
    private class Pair {
        String var;
        CFGNode node;

        Pair() {}

        Pair(final String var, final CFGNode node) {
            this.var = var;
            this.node = node;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair pair = (Pair) o;

            return var.equals(pair.var);
        }

        @Override
        public int hashCode() {
            return var.hashCode();
        }
    }

}