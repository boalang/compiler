package boa.compiler.ast.statements;

import boa.compiler.ast.Node;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.parser.Token;

/**
 * 
 * @author rdyer
 */
public class ExprStatement extends Statement {
	protected Expression e;

	public Expression getExpr() {
		return e;
	}

	public ExprStatement (final Expression e) {
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

	public ExprStatement clone() {
		final ExprStatement s = new ExprStatement(e.clone());
		copyFieldsTo(s);
		return s;
	}

	public ExprStatement setPositions(final Node first) {
		return (ExprStatement)setPositions(first.beginLine, first.beginColumn, first.endLine, first.endColumn);
	}
}
