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

	public void addOp(final Node op) {
		op.setParent(this);
		ops.add(op);
	}

	public Factor (final Operand op) {
		op.setParent(this);
		this.op = op;
	}

	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
