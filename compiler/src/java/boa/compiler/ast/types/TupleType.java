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
public class TupleType extends AbstractType {
	protected final List<Component> members = new ArrayList<Component>();

	public List<Component> getMembers() {
		return members;
	}

	public int getMembersSize() {
		return members.size();
	}

	public Component getMember(final int index) {
		return members.get(index);
	}

	public void addMember(final Component c) {
		c.setParent(this);
		members.add(c);
	}

	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
