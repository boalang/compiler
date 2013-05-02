package boa.compiler.ast.statements;

import boa.compiler.ast.Factor;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class AssignmentStatement extends Statement {
	protected Factor lhs;
	protected Expression rhs;

	public Factor getLhs() {
		return lhs;
	}

	public Expression getRhs() {
		return rhs;
	}

	public AssignmentStatement(final Factor lhs, final Expression rhs) {
		lhs.setParent(this);
		rhs.setParent(this);
		this.lhs = lhs;
		this.rhs = rhs;
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
