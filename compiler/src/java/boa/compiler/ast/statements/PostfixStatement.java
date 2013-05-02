package boa.compiler.ast.statements;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class PostfixStatement extends Statement {
	protected Expression e;
	protected String op;

	public Expression getExpr() {
		return e;
	}

	public String getOp() {
		return op;
	}

	public PostfixStatement (final Expression e, final String op) {
		e.setParent(this);
		this.e = e;
		this.op = op;
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
