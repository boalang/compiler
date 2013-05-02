package boa.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Node;
import boa.compiler.ast.Conjunction;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class Expression extends Node {
	protected Conjunction lhs;
	protected final List<Conjunction> rhs = new ArrayList<Conjunction>();

	public Conjunction getLhs() {
		return lhs;
	}

	public List<Conjunction> getRhs() {
		return rhs;
	}

	public int getRhsSize() {
		return rhs.size();
	}

	public Conjunction getRhs(final int index) {
		return rhs.get(index);
	}

	public void addRhs(final Conjunction c) {
		c.setParent(this);
		rhs.add(c);
	}

	public Expression (final Conjunction lhs) {
		lhs.setParent(this);
		this.lhs = lhs;
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
