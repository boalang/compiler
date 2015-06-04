package boa.compiler.ast.statements;

import boa.compiler.ast.Factor;
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
		if (lhs != null)
			lhs.setParent(this);
		if (rhs != null)
			rhs.setParent(this);
		this.lhs = lhs;
		this.rhs = rhs;
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

	public AssignmentStatement clone() {
		final AssignmentStatement s = new AssignmentStatement(lhs.clone(), rhs.clone());
		copyFieldsTo(s);
		return s;
	}

	public AssignmentStatement setPositions(final Node first, final Token last) {
		return (AssignmentStatement)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
