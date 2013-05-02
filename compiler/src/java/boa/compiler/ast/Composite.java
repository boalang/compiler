package boa.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

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

	public Composite (final boolean empty) {
		this.empty = empty;
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
