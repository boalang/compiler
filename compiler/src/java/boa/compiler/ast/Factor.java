package boa.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class Factor extends Node {
	protected Operand op;
	protected final List<Node> ops = new ArrayList<Node>();

	public Operand getOperand() {
		return op;
	}

	public List<Node> getOps() {
		return ops;
	}

	public int getOpsSize() {
		return ops.size();
	}

	public Node getOp(final int index) {
		return ops.get(index);
	}

	public Factor addOp(final Node op) {
		op.setParent(this);
		ops.add(op);
		return this;
	}

	public Factor (final Operand op) {
		if (op != null)
			op.setParent(this);
		this.op = op;
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

	public Factor clone() {
		final Factor f = new Factor(op.clone());
		for (final Node n : ops)
			f.addOp(n.clone());
		copyFieldsTo(f);
		return f;
	}

	public Factor setPositions(final Node first, final Node last) {
		if (last == null)
			return (Factor)setPositions(first.beginLine, first.beginColumn, first.endLine, first.endColumn);
		return (Factor)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
