package boa.compiler.ast;

import boa.compiler.ast.Node;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.parser.Token;

/**
 * 
 * @author rdyer
 */
public class UnaryFactor extends Operand {
	protected String op;
	protected Factor f;

	public String getOp() {
		return op;
	}

	public Factor getFactor() {
		return f;
	}

	public UnaryFactor (final String op, final Factor f) {
		if (f != null)
			f.setParent(this);
		this.op = op;
		this.f = f;
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

	public UnaryFactor clone() {
		final UnaryFactor uf = new UnaryFactor(op, f.clone());
		copyFieldsTo(uf);
		return uf;
	}

	public UnaryFactor setPositions(final Token first, final Node last) {
		return (UnaryFactor)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
