package boa.compiler.ast;

import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
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
	public <A> void accept(final AbstractVisitor<A> v, A arg) {
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

	/*
	public Start setPositions(final Node first, final Token last) {
		return (Start)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
	*/
}
