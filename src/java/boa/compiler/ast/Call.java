package boa.compiler.ast;

import java.util.ArrayList;
import java.util.List;

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

	public void setArgs(final List<Expression> args) {
		this.args.clear();
		for (final Expression e : args) {
			e.setParent(this);
			this.args.add(e);
		}
	}

	public Call() {
	}

	public Call(final List<Expression> args) {
		if (args != null)
			for (final Expression e : args) {
				e.setParent(this);
				this.args.add(e);
			}
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
