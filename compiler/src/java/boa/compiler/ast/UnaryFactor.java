package boa.compiler.ast;

import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

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
		f.setParent(this);
		this.op = op;
		this.f = f;
	}

	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
