package boa.compiler.ast.statements;

import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.parser.Token;

/**
 * 
 * @author rdyer
 */
public class ContinueStatement extends Statement {
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

	public ContinueStatement clone() {
		final ContinueStatement s = new ContinueStatement();
		copyFieldsTo(s);
		return s;
	}

	public ContinueStatement setPositions(final Token first, final Token last) {
		return (ContinueStatement)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
