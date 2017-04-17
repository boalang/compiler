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
package boa.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Node;
import boa.compiler.ast.Conjunction;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class Expression extends Node {
	protected Conjunction lhs;
	protected final List<Conjunction> rhs = new ArrayList<Conjunction>();

	public Conjunction getLhs() {
		return lhs;
	}

	public void setLhs(final Conjunction lhs) {
		lhs.setParent(this);
		this.lhs = lhs;
	}

	public List<Conjunction> getRhs() {
		return rhs;
	}

	public int getRhsSize() {
		return rhs.size();
	}

	public Conjunction getRhs(final int index) {
		return rhs.get(index);
	}

	public void addRhs(final Conjunction c) {
		c.setParent(this);
		rhs.add(c);
	}

	public Expression () {
		this(null);
	}

	public Expression (final Conjunction lhs) {
		if (lhs != null)
			lhs.setParent(this);
		this.lhs = lhs;
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

	public Expression clone() {
		final Expression e = new Expression(lhs.clone());
		for (final Conjunction c : rhs)
			e.addRhs(c.clone());
		copyFieldsTo(e);
		return e;
	}
}
