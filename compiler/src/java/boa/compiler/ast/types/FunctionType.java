package boa.compiler.ast.types;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Component;
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

	public FunctionType () {
	}

	public FunctionType (final AbstractType t) {
		t.setParent(this);
		this.t = t;
	}

	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
