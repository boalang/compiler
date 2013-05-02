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
	protected Statement body;

	public Expression getCondition() {
		return condition;
	}

	public Statement getBody() {
		return body;
	}

	public WhileStatement(final Expression condition, final Statement body) {
		condition.setParent(this);
		body.setParent(this);
		this.condition = condition;
		this.body = body;
	}

	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
