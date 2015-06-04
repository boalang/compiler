package boa.compiler.ast.statements;

import boa.compiler.ast.Component;
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
public class IfAllStatement extends Statement {
	protected Component var;
	protected Expression condition;
	protected Block body;

	public Component getVar() {
		return var;
	}

	public Expression getCondition() {
		return condition;
	}

	public void setCondition(final Expression condition) {
		condition.setParent(this);
		this.condition = condition;
	}

	public Block getBody() {
		return body;
	}

	public IfAllStatement(final Component var, final Expression condition, final Statement s) {
		this(var, condition, Node.ensureBlock(s));
	}

	public IfAllStatement(final Component var, final Expression condition, final Block body) {
		if (var != null)
			var.setParent(this);
		if (condition != null)
			condition.setParent(this);
		if (body != null)
			body.setParent(this);
		this.var = var;
		this.condition = condition;
		this.body = body;
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

	public IfAllStatement clone() {
		final IfAllStatement s = new IfAllStatement(var.clone(), condition.clone(), body.clone());
		copyFieldsTo(s);
		return s;
	}

	public IfAllStatement setPositions(final Token first, final Node last) {
		return (IfAllStatement)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
