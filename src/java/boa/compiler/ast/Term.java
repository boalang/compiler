package boa.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class Term extends Node {
	protected Factor lhs;
	protected final List<String> ops = new ArrayList<String>();
	protected final List<Factor> rhs = new ArrayList<Factor>();

	public Factor getLhs() {
		return lhs;
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

	public List<Factor> getRhs() {
		return rhs;
	}

	public int getRhsSize() {
		return rhs.size();
	}

	public Factor getRhs(final int index) {
		return rhs.get(index);
	}

	public void addRhs(final Factor f) {
		f.setParent(this);
		rhs.add(f);
	}

	public Term (final Factor lhs) {
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

	public Term clone() {
		final Term t = new Term(lhs.clone());
		for (final String s : ops)
			t.addOp(s);
		for (final Factor f : rhs)
			t.addRhs(f.clone());
		copyFieldsTo(t);
		return t;
	}

	public Term setPositions(final Node first, final Node last) {
		if (last == null)
			return (Term)setPositions(first.beginLine, first.beginColumn, first.endLine, first.endColumn);
		return (Term)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
