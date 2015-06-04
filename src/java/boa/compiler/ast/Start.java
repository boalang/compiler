package boa.compiler.ast;

import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;
import boa.parser.Token;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class Start extends Node {
	protected Program p;

	public Program getProgram() {
		return p;
	}

	public Start(final Program p) {
		p.setParent(this);
		this.p = p;
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

	public Start clone() {
		final Start s = new Start(p.clone());
		copyFieldsTo(s);
		return s;
	}

	public Start setPositions(final Node first, final Token last) {
		return (Start)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
