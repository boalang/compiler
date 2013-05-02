package boa.compiler.ast;

import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class Comparison extends Node {
	protected SimpleExpr lhs;
	protected String op;
	protected SimpleExpr rhs;

	public SimpleExpr getLhs() {
		return lhs;
	}

	public boolean hasOp() {
		return op != null;
	}

	public String getOp() {
		return op;
	}

	public boolean hasRhs() {
		return rhs != null;
	}

	public SimpleExpr getRhs() {
		return rhs;
	}

	public Comparison (final SimpleExpr lhs) {
		lhs.setParent(this);
		this.lhs = lhs;
	}

	public Comparison (final SimpleExpr lhs, final String op, final SimpleExpr rhs) {
		lhs.setParent(this);
		rhs.setParent(this);
		this.lhs = lhs;
		this.op = op;
		this.rhs = rhs;
	}

	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
