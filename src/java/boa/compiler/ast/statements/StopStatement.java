package boa.compiler.ast.statements;

import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class StopStatement extends Statement {
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

	public StopStatement clone() {
		final StopStatement s = new StopStatement();
		copyFieldsTo(s);
		return s;
	}

	/*
	public StopStatement setPositions(final Token first, final Token last) {
		return (StopStatement)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
	*/
}
