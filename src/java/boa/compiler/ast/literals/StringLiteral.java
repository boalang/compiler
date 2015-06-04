package boa.compiler.ast.literals;

import boa.compiler.ast.Operand;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;
import boa.parser.Token;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class StringLiteral extends Operand implements ILiteral {
	protected String literal;
	
	public String getLiteral() {
		return literal;
	}

	public StringLiteral (final boolean regex, final String literal) {
		this(literal);
		if (regex)
			this.literal = "\"" + literal.substring(1, literal.length() - 1).replace("\\", "\\\\") + "\"";
	}

	public StringLiteral (final String literal) {
		this.literal = literal;
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

	public StringLiteral clone() {
		final StringLiteral l = new StringLiteral(literal);
		copyFieldsTo(l);
		return l;
	}

	public StringLiteral setPositions(final Token first) {
		return (StringLiteral)setPositions(first.beginLine, first.beginColumn, first.endLine, first.endColumn);
	}
}
