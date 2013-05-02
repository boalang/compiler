package boa.compiler.ast;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class Pair extends Node {
	protected Expression e1;
	protected Expression e2;

	public Expression getExpr1() {
		return e1;
	}

	public Expression getExpr2() {
		return e2;
	}

	public Pair (final Expression e1, final Expression e2) {
		e1.setParent(this);
		e2.setParent(this);
		this.e1 = e1;
		this.e2 = e2;
	}

	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
