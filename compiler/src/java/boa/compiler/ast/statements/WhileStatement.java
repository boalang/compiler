package boa.compiler.ast.statements;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class WhileStatement extends Statement {
	protected Expression condition;
	protected Block body;

	public Expression getCondition() {
		return condition;
	}

	public Block getBody() {
		return body;
	}

	public WhileStatement(final Expression condition, final Block body) {
		condition.setParent(this);
		body.setParent(this);
		this.condition = condition;
		this.body = body;
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

	public WhileStatement clone() {
		final WhileStatement s = new WhileStatement(condition.clone(), body.clone());
		copyFieldsTo(s);
		return s;
	}
}
