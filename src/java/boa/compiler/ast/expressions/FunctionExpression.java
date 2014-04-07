package boa.compiler.ast.expressions;

import boa.compiler.ast.Operand;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.types.AbstractType;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class FunctionExpression extends Operand {
	protected AbstractType t;
	protected Block body;

	public AbstractType getType() {
		return t;
	}

	public Block getBody() {
		return body;
	}

	public FunctionExpression (final AbstractType t, final Block body) {
		if (t != null)
			t.setParent(this);
		if (body != null)
			body.setParent(this);
		this.t = t;
		this.body = body;
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

	public FunctionExpression clone() {
		final FunctionExpression e = new FunctionExpression(t.clone(), body.clone());
		copyFieldsTo(e);
		return e;
	}
}
