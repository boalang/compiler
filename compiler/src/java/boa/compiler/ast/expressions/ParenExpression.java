package boa.compiler.ast.expressions;

import boa.compiler.ast.Operand;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class ParenExpression extends Operand {
	protected Expression e;

	public Expression getExpression() {
		return e;
	}

	public ParenExpression (final Expression e) {
		e.setParent(this);
		this.e = e;
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

	public ParenExpression clone() {
		final ParenExpression p = new ParenExpression(e.clone());
		copyFieldsTo(p);
		return p;
	}
}
