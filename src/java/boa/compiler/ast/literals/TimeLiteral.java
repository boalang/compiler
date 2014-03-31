package boa.compiler.ast.literals;

import boa.compiler.ast.Operand;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class TimeLiteral extends Operand implements ILiteral {
	protected String literal;
	
	public String getLiteral() {
		return literal;
	}

	public TimeLiteral (final String literal) {
		this.literal = literal;
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

	public TimeLiteral clone() {
		final TimeLiteral l = new TimeLiteral(literal);
		copyFieldsTo(l);
		return l;
	}
}
