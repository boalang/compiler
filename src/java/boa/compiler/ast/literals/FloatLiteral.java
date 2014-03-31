package boa.compiler.ast.literals;

import boa.compiler.ast.Operand;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class FloatLiteral extends Operand implements ILiteral {
	protected String literal;
	
	public String getLiteral() {
		return literal;
	}

	public FloatLiteral (final String literal) {
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

	public FloatLiteral clone() {
		final FloatLiteral l = new FloatLiteral(literal);
		copyFieldsTo(l);
		return l;
	}

	/*
	public FloatLiteral setPositions(final Token first) {
		return (FloatLiteral)setPositions(first.beginLine, first.beginColumn, first.endLine, first.endColumn);
	}
	*/
}
