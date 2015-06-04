package boa.compiler.ast.types;

import boa.compiler.ast.Component;
import boa.compiler.ast.Node;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;
import boa.parser.Token;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class SetType extends AbstractType {
	protected Component value;

	public Component getValue() {
		return value;
	}

	public SetType (final Component value) {
		if (value != null)
			value.setParent(this);
		this.value = value;
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

	public SetType clone() {
		final SetType t = new SetType(value.clone());
		copyFieldsTo(t);
		return t;
	}

	public SetType setPositions(final Token first, final Node last) {
		return (SetType)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
