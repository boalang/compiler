package boa.analysis.ds;

import boa.types.Ast.Expression;
import boa.types.Ast.Statement;

public class Node {
	public Statement stmt;
	public Expression expr;
	public boa.types.Control.Node node;
	
	public Node(boa.types.Control.Node node, Statement stmt) {
		this.node = node;
		this.stmt = stmt;
		this.expr = null;
	}
	
	public Node(boa.types.Control.Node node, Expression expr) {
		this.node = node;
		this.stmt = null;
		this.expr = expr;
	}
	
	public Node(boa.types.Control.Node node) {
		this.node = node;
		this.stmt = null;
		this.expr = null;
	}
	
	public final boolean hasExpr() {
		if (expr != null) 
			return true;
		return false;
	}
	
	@Override
	public String toString() {
		String ret = "";
		if (stmt != null)
			return stmt.toString();
		else 
			return expr.toString();
	}
}
