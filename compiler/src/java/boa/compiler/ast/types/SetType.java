package boa.compiler.ast.types;

import boa.compiler.ast.Component;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class SetType extends AbstractType {
	protected Component value;

	public Component getValue() {
		return value;
	}

	public SetType (final Component value) {
		value.setParent(this);
		this.value = value;
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

	public SetType clone() {
		final SetType t = new SetType(value.clone());
		copyFieldsTo(t);
		return t;
	}
}
