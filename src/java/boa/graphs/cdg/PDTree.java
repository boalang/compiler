package boa.graphs.cdg;

import boa.graphs.cfg.CFG;
import boa.graphs.cfg.CFGNode;
import boa.runtime.BoaAbstractFixP;
import boa.runtime.BoaAbstractTraversal;
import boa.types.Ast.*;
import boa.types.Graph;

import java.util.*;

public class PDTree {

    private Set<TreeNode> nodes;
    private TreeNode entryNode;

    public PDTree(CFG cfg) throws Exception {
        nodes = new HashSet<TreeNode>();
        Map<CFGNode, Set<CFGNode>> pdom = computePostDominator(cfg);
        Map<CFGNode, CFGNode> ipdom = computeImmediatePostDominator(pdom);
        buildPDomTree(ipdom, cfg.getNodes().size());
    }

    public PDTree(final Method method) throws Exception {
        this(new CFG(method));
    }

    //Getters
    private Set<TreeNode> getNodes() {
        return nodes;
    }

    private TreeNode getEntryNode() {
        return entryNode;
    }

    /**
     * Computes post-dominators for each node in the control flow graph
     *
     * @param cfg control flow graph
     * @return map of node and corresponding set of post-dominator nodes
     * @throws Exception
     */
    private Map<CFGNode, Set<CFGNode>> computePostDominator(final CFG cfg) throws Exception {
        Set<CFGNode> nodeids = cfg.getNodes();

        final BoaAbstractTraversal pdom = new BoaAbstractTraversal<Set<CFGNode>>(true, true) {

            protected Set<CFGNode> preTraverse(final CFGNode node) throws Exception {
                Set<CFGNode> currentPDom = new HashSet<CFGNode>();

                if (node.getId() != (nodeids.size() - 1))
                    currentPDom = nodeids;

                if ((getValue(node) != null))
                    currentPDom = getValue(node);

                for (CFGNode successor : node.getSuccessorsList()) {
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

        pdom.traverse(cfg, Graph.Traversal.TraversalDirection.FORWARD, Graph.Traversal.TraversalKind.REVERSEPOSTORDER, fixp);

        return pdom.outputMapObj;
    }

    /**
     * Computes immediate post-dominator for each node
     *
     * @param pdom map of nodes and corresponding post-dominators
     * @return map of nodes and corresponding immediate post-dominator
     */
    private Map<CFGNode, CFGNode> computeImmediatePostDominator(Map<CFGNode, Set<CFGNode>> pdom) {
        //Inefficient implementation: t-complexity = O(n^3)
        //To find ipdom, we check each pdom of a node to see if it is post dominating any other
        //node. Each node should have atmost one ip-dom (last node has no immediate post dominator)
        Map<CFGNode, CFGNode> ipdom = new HashMap<CFGNode, CFGNode>();
        for (CFGNode n : pdom.keySet()) {
            for (CFGNode pd1 : pdom.get(n)) {
                boolean isIPDom = true;
                for (CFGNode pd2 : pdom.get(n)) {
                    if (!pd1.equals(pd2))
                        if ((pdom.get(pd2)).contains(pd1)) {
                            isIPDom = false;
                            break;
                        }
                }
                if (isIPDom) {
                    ipdom.put(n, pd1);
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
    private void buildPDomTree(Map<CFGNode, CFGNode> ipdoms, int stopid) {
        //Create an edge between ipdom and corresponding node.
        //Since each node can have only one ipdom, the resulting graph will form a tree
        for (CFGNode n : ipdoms.keySet()) {
            CFGNode ipdom = ipdoms.get(n);

            TreeNode src = getNode(ipdom);
            TreeNode dest = getNode(n);

            src.getChildren().add(dest);
            dest.setParent(src);

            if (src.getId() == stopid)
                entryNode = src;
        }
    }

    /**
     * Checks if a node already exists and returns it, otherwise returns a new node.
     *
     * @param cfgNode
     * @return a new tree node or an existing tree node
     */
    private TreeNode getNode(CFGNode cfgNode) {
        TreeNode node = new TreeNode(cfgNode);
        if (nodes.contains(node)) {
            for (TreeNode n : nodes) {
                if (n == node)
                    return n;
            }
        }
        nodes.add(node);

        return node;
    }

}