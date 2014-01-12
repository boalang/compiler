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
		this(lhs, null, null);
	}

	public Comparison (final SimpleExpr lhs, final String op, final SimpleExpr rhs) {
		if (lhs != null)
			lhs.setParent(this);
		if (rhs != null)
			rhs.setParent(this);
		this.lhs = lhs;
		this.op = op;
		this.rhs = rhs;
	}

	/** {@inheritDoc} */
	@Override
	public <A> void accept(final AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public void accept(final AbstractVisitorNoArg v) {
		v.visit(this);
	}

	public Comparison clone() {
		final Comparison c;
		if (hasOp())
			c = new Comparison(lhs.clone(), op, rhs.clone());
		else
			c = new Comparison(lhs.clone());
		copyFieldsTo(c);
		return c;
	}
}
