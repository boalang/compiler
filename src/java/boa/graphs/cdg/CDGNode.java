package boa.graphs.cdg;


import boa.graphs.cfg.CFGNode;
import boa.types.Ast.*;

import java.util.*;

public class CDGNode implements Comparable<CDGNode> {

    private int id;
    private String pid;
    private Statement stmt;
    private Expression expr;

    private Set<CDGEdge> inEdges = new HashSet<CDGEdge>();
    private Set<CDGEdge> outEdges = new HashSet<CDGEdge>();
    private List<CDGNode> successors = new java.util.ArrayList<CDGNode>();
    private List<CDGNode> predecessors = new java.util.ArrayList<CDGNode>();

    public CDGNode(final TreeNode node) {
        this.id = node.getId();
        this.pid = node.getPid();
        this.stmt = node.getStmt();
        this.expr = node.getExpr();
    }

    public CDGNode(int id) {
        this.id = id;
    }

    //Setters
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

    public void addSuccessor(final CDGNode node) {
        successors.add(node);
    }

    public void addPredecessor(final CDGNode node) {
        predecessors.add(node);
    }

    public void addInEdges(final CDGEdge edge) {
        inEdges.add(edge);
    }

    public void addOutEdges(final CDGEdge edge) {
        inEdges.add(edge);
    }

    //Getters
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

    public Set<CDGEdge> getInEdges() {
        return inEdges;
    }

    public Set<CDGEdge> getOutEdges() {
        return outEdges;
    }

    public List<CDGNode> getSuccessors() {
        return successors;
    }

    public List<CDGNode> getPredecessors() {
        return predecessors;
    }

    @Override
    public int compareTo(final CDGNode node) {
        return node.id - this.id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CDGNode cdgNode = (CDGNode) o;

        return id == cdgNode.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
