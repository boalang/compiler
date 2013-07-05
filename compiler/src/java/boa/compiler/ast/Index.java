package boa.compiler.ast;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class Index extends Node {
	protected Expression start;
	protected Expression end;

	public Expression getStart() {
		return start;
	}

	public boolean hasEnd() {
		return end != null;
	}

	public Expression getEnd() {
		return end;
	}

	public Index (final Expression start) {
		this(start, null);
	}

	public Index (final Expression start, final Expression end) {
		start.setParent(this);
		if (end != null)
			end.setParent(this);
		this.start = start;
		this.end = end;
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

	public Index clone() {
		final Index i;
		if (hasEnd())
			i = new Index(start.clone(), end.clone());
		else
			i = new Index(start.clone());
		copyFieldsTo(i);
		return i;
	}
}
