package boa.compiler.ast;

import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class Selector extends Node {
	protected Identifier id;

	public Identifier getId() {
		return id;
	}

	public Selector (final Identifier id) {
		id.setParent(this);
		this.id = id;
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

	public Selector clone() {
		final Selector s = new Selector(id.clone());
		copyFieldsTo(s);
		return s;
	}
}
