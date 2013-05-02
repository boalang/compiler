package boa.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Identifier;
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

	public boolean hasWeight() {
		return weight != null;
	}

	public Expression getWeight() {
		return weight;
	}

	public EmitStatement (final Identifier id, final Expression value) {
		id.setParent(this);
		value.setParent(this);
		this.id = id;
		this.value = value;
	}

	public EmitStatement (final Identifier id, final Expression value, final Expression weight) {
		id.setParent(this);
		value.setParent(this);
		weight.setParent(this);
		this.id = id;
		this.value = value;
		this.weight = weight;
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
