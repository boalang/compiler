package boa.compiler.ast.types;

import boa.compiler.ast.Component;
import boa.compiler.ast.Node;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class MapType extends AbstractType {
	protected Component index;
	protected Component value;

	public Component getIndex() {
		return index;
	}

	public Component getValue() {
		return value;
	}

	public MapType (final Component index, final Component value) {
		if (index != null)
			index.setParent(this);
		if (value != null)
			value.setParent(this);
		this.index = index;
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

	public MapType clone() {
		final MapType t = new MapType(index.clone(), value.clone());
		copyFieldsTo(t);
		return t;
	}

	/*
	public MapType setPositions(final Token first, final Node last) {
		return (MapType)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
	*/
}
