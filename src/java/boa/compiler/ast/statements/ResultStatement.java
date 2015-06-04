package boa.compiler.ast.statements;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;
import boa.parser.Token;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class ResultStatement extends Statement {
	protected Expression expr;

	public Expression getExpr() {
		return expr;
	}

	public ResultStatement(final Expression expr) {
		if (expr != null)
			expr.setParent(this);
		this.expr = expr;
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

	public ResultStatement clone() {
		final ResultStatement s = new ResultStatement(expr.clone());
		copyFieldsTo(s);
		return s;
	}

	public ResultStatement setPositions(final Token first, final Token last) {
		return (ResultStatement)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
