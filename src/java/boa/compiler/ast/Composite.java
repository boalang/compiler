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
public class Composite extends Operand {
	protected boolean empty;
	protected final List<Pair> pairs = new ArrayList<Pair>();
	protected final List<Expression> exprs = new ArrayList<Expression>();

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(final boolean empty) {
		this.empty = empty;
	}

	public List<Pair> getPairs() {
		return pairs;
	}

	public int getPairsSize() {
		return pairs.size();
	}

	public Pair getPair(final int index) {
		return pairs.get(index);
	}

	public void addPair(final Pair p) {
		p.setParent(this);
		pairs.add(p);
	}

	public List<Expression> getExprs() {
		return exprs;
	}

	public int getExprsSize() {
		return exprs.size();
	}

	public Expression getExpr(final int index) {
		return exprs.get(index);
	}

	public void addExpr(final Expression e) {
		e.setParent(this);
		exprs.add(e);
	}

	public Composite () {
		this(false);
	}

	public Composite (final List<Expression> exprs) {
		if (exprs != null)
			for (final Expression e : exprs) {
				e.setParent(this);
				this.exprs.add(e);
			}
	}

	public Composite (final boolean empty) {
		this.empty = empty;
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

	public Composite clone() {
		final Composite c = new Composite(empty);
		for (final Expression e : exprs)
			c.addExpr(e.clone());
		for (final Pair p : pairs)
			c.addPair(p.clone());
		copyFieldsTo(c);
		return c;
	}

	public Composite setPositions(final Token first, final Token last) {
		return (Composite)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
