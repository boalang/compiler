package boa.compiler.ast.types;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Component;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
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

	public void setId(final Identifier id) {
		id.setParent(this);
		this.id = id;
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

	public void setArgs(final List<Expression> args) {
		this.args.clear();
		for (final Expression e : args) {
			e.setParent(this);
			this.args.add(e);
		}
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

	public void setType(final Component t) {
		t.setParent(this);
		this.t = t;
	}

	public boolean hasWeight() {
		return weight != null;
	}

	public Component getWeight() {
		return weight;
	}

	public void setWeight(final Component weight) {
		weight.setParent(this);
		this.weight = weight;
	}

	public OutputType (final Identifier id) {
		this(id, null, null);
	}

	public OutputType (final Identifier id, final Component t) {
		this(id, t, null);
	}

	public OutputType (final Identifier id, final Component t, final Component weight) {
		if (id != null)
			id.setParent(this);
		if (t != null)
			t.setParent(this);
		if (weight != null)
			weight.setParent(this);
		this.id = id;
		this.t = t;
		this.weight = weight;
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

	public OutputType clone() {
		final OutputType o;
		if (hasWeight())
			o = new OutputType(id.clone(), t.clone(), weight.clone());
		else
			o = new OutputType(id.clone(), t.clone());
		for (final Expression e : args)
			o.addArg(e.clone());
		for (final Component c : indices)
			o.addIndice(c.clone());
		copyFieldsTo(o);
		return o;
	}
}
