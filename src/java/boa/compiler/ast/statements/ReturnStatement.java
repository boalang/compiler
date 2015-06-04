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
public class ReturnStatement extends Statement {
	protected Expression expr;

	public boolean hasExpr() {
		return expr != null;
	}

	public Expression getExpr() {
		return expr;
	}

	public void setExpr(final Expression expr) {
		expr.setParent(this);
		this.expr = expr;
	}

	public ReturnStatement() {
		this(null);
	}

	public ReturnStatement(final Expression expr) {
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

	public ReturnStatement clone() {
		final ReturnStatement s;
		if (hasExpr())
			s = new ReturnStatement(expr.clone());
		else
			s = new ReturnStatement();
		copyFieldsTo(s);
		return s;
	}

	public ReturnStatement setPositions(final Token first, final Token last) {
		return (ReturnStatement)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
