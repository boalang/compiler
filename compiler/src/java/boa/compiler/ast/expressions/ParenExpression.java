package boa.compiler.ast.expressions;

import boa.compiler.ast.Operand;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class ParenExpression extends Operand {
	protected Expression e;

	public Expression getExpression() {
		return e;
	}

	public ParenExpression (final Expression e) {
		e.setParent(this);
		this.e = e;
	}

	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
