package boa.compiler.ast.statements;

import boa.compiler.ast.Component;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class ForeachStatement extends Statement {
	protected Component var;
	protected Expression condition;
	protected Statement body;

	public Component getVar() {
		return var;
	}

	public Expression getCondition() {
		return condition;
	}

	public Statement getBody() {
		return body;
	}

	public ForeachStatement(final Component var, final Expression condition, final Statement body) {
		var.setParent(this);
		condition.setParent(this);
		body.setParent(this);
		this.var = var;
		this.condition = condition;
		this.body = body;
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
