package boa.compiler.ast;

import boa.compiler.ast.types.AbstractType;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class Component extends AbstractType {
	protected Identifier id;
	protected AbstractType t;

	public boolean hasIdentifier() {
		return id != null;
	}

	public Identifier getIdentifier() {
		return id;
	}

	public AbstractType getType() {
		return t;
	}

	public Component (final AbstractType t) {
		this(null, t);
	}

	public Component (final Identifier id, final AbstractType t) {
		if (id != null)
			id.setParent(this);
		t.setParent(this);
		this.id = id;
		this.t = t;
	}

	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
