package boa.compiler.ast.statements;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class ResultStatement extends Statement {
	protected Expression expr;

	public Expression getExpr() {
		return expr;
	}

	public ResultStatement(final Expression expr) {
		expr.setParent(this);
		this.expr = expr;
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

	public ResultStatement clone() {
		final ResultStatement s = new ResultStatement(expr.clone());
		copyFieldsTo(s);
		return s;
	}
}
