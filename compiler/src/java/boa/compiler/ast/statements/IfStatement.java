package boa.compiler.ast.statements;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class IfStatement extends Statement {
	protected Expression condition;
	protected Statement body;
	protected Statement elseBody;

	public Expression getCondition() {
		return condition;
	}

	public Statement getBody() {
		return body;
	}

	public boolean hasElse() {
		return elseBody != null;
	}

	public Statement getElse() {
		return elseBody;
	}

	public IfStatement(final Expression condition, final Statement body) {
		condition.setParent(this);
		body.setParent(this);
		this.condition = condition;
		this.body = body;
	}

	public IfStatement(final Expression condition, final Statement body, final Statement elseBody) {
		condition.setParent(this);
		body.setParent(this);
		elseBody.setParent(this);
		this.condition = condition;
		this.body = body;
		this.elseBody = elseBody;
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
