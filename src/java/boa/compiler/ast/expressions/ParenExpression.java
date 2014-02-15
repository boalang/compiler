package boa.compiler.ast.expressions;

import boa.compiler.ast.Operand;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.parser.Token;

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
		if (e != null)
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

	public ParenExpression setPositions(final Token first, final Token last) {
		return (ParenExpression)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
