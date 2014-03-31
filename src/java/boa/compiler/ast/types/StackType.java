package boa.compiler.ast.types;

import boa.compiler.ast.Component;
import boa.compiler.ast.Node;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class StackType extends AbstractType {
	protected Component value;

	public Component getValue() {
		return value;
	}

	public StackType (final Component value) {
		if (value != null)
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

	public StackType clone() {
		final StackType t = new StackType(value.clone());
		copyFieldsTo(t);
		return t;
	}

	/*
	public StackType setPositions(final Token first, final Node last) {
		return (StackType)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
	*/
}
