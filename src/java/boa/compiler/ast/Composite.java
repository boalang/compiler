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
package boa.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class Composite extends Operand {
	protected boolean empty;
	protected final List<Pair> pairs = new ArrayList<Pair>();
	protected final List<Expression> exprs = new ArrayList<Expression>();

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(final boolean empty) {
		this.empty = empty;
	}

	public List<Pair> getPairs() {
		return pairs;
	}

	public int getPairsSize() {
		return pairs.size();
	}

	public Pair getPair(final int index) {
		return pairs.get(index);
	}

	public void addPair(final Pair p) {
		p.setParent(this);
		pairs.add(p);
	}

	public List<Expression> getExprs() {
		return exprs;
	}

	public int getExprsSize() {
		return exprs.size();
	}

	public Expression getExpr(final int index) {
		return exprs.get(index);
	}

	public void addExpr(final Expression e) {
		e.setParent(this);
		exprs.add(e);
	}

	public Composite () {
		this(false);
	}

	public Composite (final List<Expression> exprs) {
		if (exprs != null)
			for (final Expression e : exprs) {
				e.setParent(this);
				this.exprs.add(e);
			}
	}

	public Composite (final boolean empty) {
		this.empty = empty;
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

	@Override
	public void replaceExpression(final Expression oldExp, final Expression newExp) {
		for (int i = 0; i < exprs.size(); i++) {
			if (oldExp == exprs.get(i)) {
				newExp.setParent(this);
				exprs.set(i, newExp);
			}
		}
	}

	public Composite clone() {
		final Composite c = new Composite(empty);
		for (final Expression e : exprs)
			c.addExpr(e.clone());
		for (final Pair p : pairs)
			c.addPair(p.clone());
		copyFieldsTo(c);
		return c;
	}
}
