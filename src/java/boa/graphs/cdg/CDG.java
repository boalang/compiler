package boa.graphs.cdg;


import boa.functions.BoaIntrinsics;
import boa.graphs.cfg.CFG;
import boa.graphs.cfg.CFGNode;
import boa.runtime.BoaAbstractFixP;
import boa.runtime.BoaAbstractTraversal;
import boa.types.Ast.*;
import boa.types.Graph;

import java.util.*;

public class CDG {

    public CDG(final Method method) throws Exception {
        CFG cfg = new CFG(method);
        Map<Integer, Set<CFGNode>> pdomTree = postDominator(cfg);
        //dominatorFrontier();
        //mapEdges();
    }

    /**
     * Constructs a post-dominator tree for a given control flow graph
     *
     * @param cfg control flow graph
     * @return
     * @throws Exception
     */
    private Map<Integer, Set<CFGNode>> postDominator(CFG cfg) throws Exception {
        final BoaAbstractTraversal pDom = new BoaAbstractTraversal<HashSet<CFGNode>>(true, true) {

            protected Set<CFGNode> preTraverse(final CFGNode node) throws Exception {
                Set<CFGNode> currentPDom = new HashSet<CFGNode>();

                if (node.getId() !=  (cfg.getNodes().size() - 1))
                    currentPDom =  cfg.getNodes() ;

                if ((getValue(node) != null))
                    currentPDom = getValue(node);

                for (CFGNode successor: node.getSuccessorsList()) {
                    if (successor != null) {
                        Set<CFGNode> succPDom = getValue(successor);
                        if (succPDom != null)
                            currentPDom.retainAll(succPDom);
                    }
                }

                if (node != null)
                    currentPDom.add(node);

                return currentPDom;
            }

            @Override
            public void traverse(final CFGNode node, boolean flag) throws Exception {
                if(flag) {
                    currentResult = (HashSet<CFGNode>)preTraverse(node);
                    outputMapObj.put(node.getId(), currentResult);
                }
                else
                    outputMapObj.put(node.getId(), (HashSet<CFGNode>)preTraverse(node));
            }

        };

        BoaAbstractFixP fixp = new BoaAbstractFixP()
        {
             boolean invoke1(final HashSet<CFGNode> current, final HashSet<CFGNode> previous) throws Exception {
                 Set<CFGNode> curr = new HashSet<CFGNode>(current);
                 curr.removeAll(previous);
                 if (curr.size() == 0)
                     return  true ;
                 return  false ;
            }

            @Override
            public boolean invoke(Object current, Object previous) throws Exception{
                return invoke1((HashSet<CFGNode>)current, (HashSet<CFGNode>)previous);
            }
        };

        pDom.traverse( cfg , Graph.Traversal.TraversalDirection.FORWARD, Graph.Traversal.TraversalKind.REVERSEPOSTORDER, fixp);

        return pDom.outputMapObj;
    }

}