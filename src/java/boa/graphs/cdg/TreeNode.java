package boa.graphs.cdg;

import boa.types.Ast.*;
import boa.graphs.cfg.CFGNode;

import java.util.*;

public class TreeNode implements Comparable<TreeNode> {

    private TreeNode parent;
    private int id;
    private String pid;
    private Statement stmt;
    private Expression expr;

    private Set<TreeNode> children = new HashSet<TreeNode>();

    public TreeNode(CFGNode node) {
        this.id = node.getId();
        this.pid = node.getPid();
        this.stmt = node.getStmt();
        this.expr = node.getExpr();
    }

    public TreeNode(int id) {
        this.id = id;
    }

    //Setters
    public void setParent(final TreeNode parent) {
        this.parent = parent;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPid(final String pid) {
        this.pid = pid;
    }

    public void setStmt(final Statement stmt) {
        this.stmt = stmt;
    }

    public void setExpr(final Expression expr) {
        this.expr = expr;
    }

    public void addChild(final TreeNode node) {
        children.add(node);
    }

    //Getters
    public TreeNode getParent() {
        return parent;
    }

    public int getId() {
        return id;
    }

    public String getPid() {
        return pid;
    }

    public Statement getStmt() {
        return stmt;
    }

    public Expression getExpr() {
        return expr;
    }

    public Set<TreeNode> getChildren() {
        return children;
    }

    @Override
    public int compareTo(final TreeNode node) {
        return node.id - this.id;
    }

    @Override
    public boolean equals(final Object o) {
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