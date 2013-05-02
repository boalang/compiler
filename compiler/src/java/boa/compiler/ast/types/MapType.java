package boa.compiler.ast.types;

import boa.compiler.ast.Component;
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
		index.setParent(this);
		value.setParent(this);
		this.index = index;
		this.value = value;
	}

	/** {@inheritDoc} */
	@Override
	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
