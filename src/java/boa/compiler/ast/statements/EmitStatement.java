package boa.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class EmitStatement extends Statement {
	protected Identifier id;
	protected final List<Expression> indices = new ArrayList<Expression>();
	protected Expression value;
	protected Expression weight;

	public Identifier getId() {
		return id;
	}

	public List<Expression> getIndices() {
		return indices;
	}

	public int getIndicesSize() {
		return indices.size();
	}

	public Expression getIndice(final int index) {
		return indices.get(index);
	}

	public void addIndice(final Expression e) {
		e.setParent(this);
		indices.add(e);
	}

	public Expression getValue() {
		return value;
	}

	public void setValue(final Expression value) {
		value.setParent(this);
		this.value = value;
	}

	public boolean hasWeight() {
		return weight != null;
	}

	public Expression getWeight() {
		return weight;
	}

	public void setWeight(final Expression weight) {
		weight.setParent(this);
		this.weight = weight;
	}

	public EmitStatement (final Identifier id) {
		this(id, null, null);
	}

	public EmitStatement (final Identifier id, final Expression value) {
		this(id, value, null);
	}

	public EmitStatement (final Identifier id, final Expression value, final Expression weight) {
		if (id != null)
			id.setParent(this);
		if (value != null)
			value.setParent(this);
		if (weight != null)
			weight.setParent(this);
		this.id = id;
		this.value = value;
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

	public EmitStatement clone() {
		final EmitStatement e = new EmitStatement(id.clone(), value.clone());
		if (hasWeight())
			e.weight = weight.clone();
		for (final Expression i : indices)
			e.addIndice(i.clone());
		copyFieldsTo(e);
		return e;
	}

	/*
	public EmitStatement setPositions(final Node first, final Token last) {
		return (EmitStatement)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
	*/
}
