package boa.compiler.ast.types;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Component;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class OutputType extends AbstractType {
	protected Identifier id;
	protected final List<Expression> args = new ArrayList<Expression>();
	protected final List<Component> indices = new ArrayList<Component>();
	protected Component t;
	protected Component weight;

	public Identifier getId() {
		return id;
	}

	public List<Expression> getArgs() {
		return args;
	}

	public int getArgsSize() {
		return args.size();
	}

	public Expression getArg(final int index) {
		return args.get(index);
	}

	public void addArg(final Expression e) {
		e.setParent(this);
		args.add(e);
	}

	public List<Component> getIndices() {
		return indices;
	}

	public int getIndicesSize() {
		return indices.size();
	}

	public Component getIndice(final int index) {
		return indices.get(index);
	}

	public void addIndice(final Component c) {
		c.setParent(this);
		indices.add(c);
	}

	public Component getType() {
		return t;
	}

	public boolean hasWeight() {
		return weight != null;
	}

	public Component getWeight() {
		return weight;
	}

	public OutputType (final Identifier id, final Component t) {
		id.setParent(this);
		t.setParent(this);
		this.id = id;
		this.t = t;
	}

	public OutputType (final Identifier id, final Component t, final Component weight) {
		id.setParent(this);
		t.setParent(this);
		weight.setParent(this);
		this.id = id;
		this.t = t;
		this.weight = weight;
	}

	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
