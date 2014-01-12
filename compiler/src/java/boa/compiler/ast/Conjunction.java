package boa.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class Conjunction extends Node {
	protected Comparison lhs;
	protected final List<String> ops = new ArrayList<String>();
	protected final List<Comparison> rhs = new ArrayList<Comparison>();

	public Comparison getLhs() {
		return lhs;
	}

	public void setLhs(final Comparison lhs) {
		lhs.setParent(this);
		this.lhs = lhs;
	}

	public List<String> getOps() {
		return ops;
	}

	public int getOpsSize() {
		return ops.size();
	}

	public String getOp(final int index) {
		return ops.get(index);
	}

	public void addOp(final String s) {
		ops.add(s);
	}

	public List<Comparison> getRhs() {
		return rhs;
	}

	public int getRhsSize() {
		return rhs.size();
	}

	public Comparison getRhs(final int index) {
		return rhs.get(index);
	}

	public void addRhs(final Comparison c) {
		c.setParent(this);
		rhs.add(c);
	}

	public Conjunction () {
		this(null);
	}

	public Conjunction (final Comparison lhs) {
		if (lhs != null)
			lhs.setParent(this);
		this.lhs = lhs;
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

	public Conjunction clone() {
		final Conjunction c = new Conjunction(lhs.clone());
		for (final Comparison c2 : rhs)
			c.addRhs(c2.clone());
		copyFieldsTo(c);
		return c;
	}
}
