package boa.compiler.ast.types;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Component;
import boa.compiler.ast.Node;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class FunctionType extends AbstractType {
	protected final List<Component> args = new ArrayList<Component>();
	protected AbstractType t;

	public List<Component> getArgs() {
		return args;
	}

	public int getArgsSize() {
		return args.size();
	}

	public Component getArg(final int index) {
		return args.get(index);
	}

	public void addArg(final Component c) {
		c.setParent(this);
		args.add(c);
	}

	public boolean hasType() {
		return t != null;
	}

	public AbstractType getType() {
		return t;
	}

	public void setType(final AbstractType t) {
		t.setParent(this);
		this.t = t;
	}

	public FunctionType () {
	}

	public FunctionType (final AbstractType t) {
		if (t != null)
			t.setParent(this);
		this.t = t;
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

	public FunctionType clone() {
		final FunctionType f;
		if (hasType())
			f = new FunctionType(t.clone());
		else
			f = new FunctionType();
		for (final Component c : args)
			f.addArg(c.clone());
		copyFieldsTo(f);
		return f;
	}

	/*
	public FunctionType setPositions(final Token first, final Token last) {
		return (FunctionType)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
	*/

	public FunctionType setEnd(final Node last) {
		return (FunctionType)setPositions(beginLine, beginColumn, last.endLine, last.endColumn);
	}
}
