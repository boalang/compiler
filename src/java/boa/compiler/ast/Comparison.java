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

import boa.compiler.ast.Node;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class Comparison extends Node {
	protected SimpleExpr lhs;
	protected String op;
	protected SimpleExpr rhs;

	public SimpleExpr getLhs() {
		return lhs;
	}

	public boolean hasOp() {
		return op != null;
	}

	public String getOp() {
		return op;
	}

	public boolean hasRhs() {
		return rhs != null;
	}

	public SimpleExpr getRhs() {
		return rhs;
	}

	public Comparison (final SimpleExpr lhs) {
		this(lhs, null, null);
	}

	public Comparison (final SimpleExpr lhs, final String op, final SimpleExpr rhs) {
		if (lhs != null)
			lhs.setParent(this);
		if (rhs != null)
			rhs.setParent(this);
		this.lhs = lhs;
		this.op = op;
		this.rhs = rhs;
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

	public Comparison clone() {
		final Comparison c;
		if (hasOp())
			c = new Comparison(lhs.clone(), op, rhs.clone());
		else
			c = new Comparison(lhs.clone());
		copyFieldsTo(c);
		return c;
	}
}
