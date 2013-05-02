package boa.compiler.ast.statements;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class ForStatement extends Statement {
	protected Statement init;
	protected Expression condition;
	protected Statement update;
	protected Statement body;

	public boolean hasInit() {
		return init != null;
	}

	public Statement getInit() {
		return init;
	}

	public boolean hasCondition() {
		return condition != null;
	}

	public Expression getCondition() {
		return condition;
	}

	public boolean hasUpdate() {
		return update != null;
	}

	public Statement getUpdate() {
		return update;
	}

	public Statement getBody() {
		return body;
	}

	public ForStatement(final Statement body) {
		body.setParent(this);
		this.body = body;
	}

	public ForStatement(final Statement init, final Expression condition, final Statement update, final Statement body) {
		if (init != null)
			init.setParent(this);
		if (condition != null)
			condition.setParent(this);
		if (update != null)
			update.setParent(this);
		body.setParent(this);
		this.init = init;
		this.condition = condition;
		this.update = update;
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
