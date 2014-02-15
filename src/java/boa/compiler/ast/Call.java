package boa.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.parser.Token;

/**
 * 
 * @author rdyer
 */
public class Call extends Node {
	protected final List<Expression> args = new ArrayList<Expression>();

	public List<Expression> getArgs() {
		return args;
	}

	public int getArgsSize() {
		return args.size();
	}

	public Expression getArg(final int index) {
		return args.get(index);
	}

	public Call addArg(final Expression e) {
		e.setParent(this);
		args.add(e);
		return this;
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

	public Call clone() {
		final Call c = new Call();
		for (final Expression e : args)
			c.addArg(e.clone());
		copyFieldsTo(c);
		return c;
	}

	public Call setPositions(final Token first, final Token last) {
		return (Call)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
