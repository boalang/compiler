package boa.compiler.ast.expressions;

import boa.compiler.ast.Node;
import boa.compiler.ast.Operand;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.types.FunctionType;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class FunctionExpression extends Operand {
	protected FunctionType t;
	protected Block body;

	public FunctionType getType() {
		return t;
	}

	public Block getBody() {
		return body;
	}

	public FunctionExpression (final FunctionType t, final Block body) {
		if (t != null)
			t.setParent(this);
		if (body != null)
			body.setParent(this);
		this.t = t;
		this.body = body;
	}

	/** {@inheritDoc} */
	@Override
	public <T,A> T accept(final AbstractVisitor<T,A> v, A arg) {
		return v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public <A> void accept(final AbstractVisitorNoReturn<A> v, A arg) {
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

	public FunctionExpression setPositions(final Node first, final Node last) {
		return (FunctionExpression)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
