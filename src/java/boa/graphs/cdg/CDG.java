package boa.graphs.cdg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import boa.graphs.cfg.CFG;
import boa.graphs.cfg.CFGEdge;
import boa.graphs.cfg.CFGNode;
import boa.graphs.trees.PDTree;
import boa.graphs.trees.TreeNode;
import boa.types.Ast.Method;
import boa.types.Control;

public class CDG {

    private Method md;
    private String class_name;
    private CDGNode entryNode;
    private Set<CDGNode> nodes = new HashSet<CDGNode>();

    public CDG(final CFG cfg) throws Exception {
        this.md = cfg.md;
        this.class_name = cfg.class_name;
        PDTree pdTree = new PDTree(cfg);
        constructCDG(pdTree, cfg);
    }

    public CDG(final Method method) throws Exception {
        this(new CFG(method));
    }

    //Getters
    private Method getMd() {
        return md;
    }

    private String getClass_name() {
        return class_name;
    }

    public CDGNode getEntryNode() {
        return entryNode;
    }

    public Set<CDGNode> getNodes() {
        return nodes;
    }

    /**
     * Builds a Control Dependence Graph using the post dominator tree and control edges
     *
     * @param pdTree post dominator tree
     * @param cfg control flow graph
     */
    private void constructCDG(final PDTree pdTree, final CFG cfg) {
        Map<Integer[], String> controlEdges = new HashMap<Integer[], String>();
        for (CFGNode n: cfg.getNodes()) {
            if (n.getKind() == Control.CFGNode.CFGNodeType.CONTROL)
            for (CFGEdge e: n.outEdges)
                controlEdges.put(new Integer[]{e.getSrc().getId(), e.getDest().getId()}, e.label());
        }
        controlEdges.put(new Integer[]{cfg.getNodes().size(), 0}, "T");

        int graphSize = pdTree.getNodes().size();

        for (Integer[] enodes: controlEdges.keySet()) {
            TreeNode src = pdTree.getNode(enodes[0]);
            TreeNode dest = pdTree.getNode(enodes[1]);
            TreeNode srcParent = pdTree.getNode(enodes[0]).getParent();
            CDGNode source = new CDGNode(src);

            while (!srcParent.equals(dest)) {
                CDGNode destination = getNode(dest);
                source.addSuccessor(destination);
                destination.addPredecessor(source);

                CDGEdge edge = new CDGEdge(source, destination, controlEdges.get(enodes));
                source.addOutEdges(edge);
                destination.addInEdges(edge);

                dest = dest.getParent();

                if (source.getId() == graphSize-1)
                    entryNode = source;
            }
        }
    }

    /**
     * Checks if a node already exists and returns it, otherwise returns a new node.
     *
     * @param treeNode a post dominator tree node
     * @return a new tree node or an existing tree node
     */
    private CDGNode getNode(final TreeNode treeNode) {
        CDGNode node = new CDGNode(treeNode);
        if (nodes.contains(node)) {
            for (CDGNode n : nodes) {
                if (n == node)
                    return n;
            }
        }
        nodes.add(node);

        return node;
    }
}