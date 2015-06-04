package boa.compiler.ast.statements;

import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;
import boa.parser.Token;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class StopStatement extends Statement {
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

	public StopStatement clone() {
		final StopStatement s = new StopStatement();
		copyFieldsTo(s);
		return s;
	}

	public StopStatement setPositions(final Token first, final Token last) {
		return (StopStatement)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
