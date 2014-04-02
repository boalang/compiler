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

	public void setIdentifier(final Identifier id) {
		id.setParent(this);
		this.id = id;
	}

	public AbstractType getType() {
		return t;
	}

	public void setType(final AbstractType t) {
		t.setParent(this);
		this.t = t;
	}

	public Component () {
	}

	public Component (final AbstractType t) {
		this(null, t);
	}

	public Component (final Identifier id, final AbstractType t) {
		if (id != null)
			id.setParent(this);
		if (t != null)
			t.setParent(this);
		this.id = id;
		this.t = t;
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

	public Component clone() {
		final Component c = new Component(id.clone(), t.clone());
		copyFieldsTo(c);
		return c;
	}
}
