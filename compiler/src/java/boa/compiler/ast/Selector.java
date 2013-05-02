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

	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
