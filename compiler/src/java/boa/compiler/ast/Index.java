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
		start.setParent(this);
		this.start = start;
	}

	public Index (final Expression start, final Expression end) {
		start.setParent(this);
		end.setParent(this);
		this.start = start;
		this.end = end;
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
