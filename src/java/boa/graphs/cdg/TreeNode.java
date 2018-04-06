package boa.graphs.cdg;

import boa.graphs.cfg.CFGNode;

import java.util.ArrayList;
import java.util.List;

public class TreeNode implements Comparable<TreeNode> {

    private TreeNode parent;
    private int id;
    private CFGNode node;
    private List<TreeNode> children = new ArrayList<>();

    public TreeNode(CFGNode node) {
        this.id = node.getId();
        this.node = node;
    }

    //Getters
    public TreeNode getParent() {
        return parent;
    }

    public int getId() {
        return id;
    }

    public CFGNode getNode() {
        return node;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    //Setters
    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(final TreeNode node) {
        return node.id - this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeNode treeNode = (TreeNode) o;

        return id == treeNode.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}