package boa.compiler.ast.statements;

import boa.compiler.ast.Node;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.parser.Token;

/**
 * 
 * @author rdyer
 */
public class ForStatement extends Statement {
	protected Statement init;
	protected Expression condition;
	protected Statement update;
	protected Block body;

	public boolean hasInit() {
		return init != null;
	}

	public Statement getInit() {
		return init;
	}

	public void setInit(final Statement init) {
		init.setParent(this);
		this.init = init;
	}

	public boolean hasCondition() {
		return condition != null;
	}

	public Expression getCondition() {
		return condition;
	}

	public void setCondition(final Expression condition) {
		condition.setParent(this);
		this.condition = condition;
	}

	public boolean hasUpdate() {
		return update != null;
	}

	public Statement getUpdate() {
		return update;
	}

	public void setUpdate(final Statement update) {
		update.setParent(this);
		this.update = update;
	}

	public Block getBody() {
		return body;
	}

	public void setBody(final Statement s) {
		setBody(Node.ensureBlock(s));
	}

	public void setBody(final Block body) {
		body.setParent(this);
		this.body = body;
	}

	public ForStatement() {
	}

	public ForStatement(final Statement init, final Expression condition, final Statement update, final Statement s) {
		this(init, condition, update, Node.ensureBlock(s));
	}

	public ForStatement(final Statement init, final Expression condition, final Statement update, final Block body) {
		if (init != null)
			init.setParent(this);
		if (condition != null)
			condition.setParent(this);
		if (update != null)
			update.setParent(this);
		if (body != null)
			body.setParent(this);
		this.init = init;
		this.condition = condition;
		this.update = update;
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

	public ForStatement clone() {
		final ForStatement f = new ForStatement(null, null, null, body.clone());
		if (hasInit())
			f.init = init.clone();
		if (hasCondition())
			f.condition = condition.clone();
		if (hasUpdate())
			f.update = update.clone();
		copyFieldsTo(f);
		return f;
	}

	public ForStatement setPositions(final Token first, final Node last) {
		return (ForStatement)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
