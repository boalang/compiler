package boa.compiler.ast.statements;

import boa.compiler.ast.Node;
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
		if (e != null)
			e.setParent(this);
		this.e = e;
		this.op = op;
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

	public PostfixStatement clone() {
		final PostfixStatement s = new PostfixStatement(e.clone(), op);
		copyFieldsTo(s);
		return s;
	}

	public PostfixStatement setPositions(final Node first, final Token last) {
		return (PostfixStatement)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
