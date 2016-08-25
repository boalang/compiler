/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Identifier;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
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
	public <T,A> T accept(final AbstractVisitor<T,A> v, A arg) {
		return v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public <A> void accept(final AbstractVisitorNoReturn<A> v, A arg) {
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
}
