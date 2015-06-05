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
import boa.compiler.ast.Term;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class SimpleExpr extends Node {
	protected Term lhs;
	protected final List<String> ops = new ArrayList<String>();
	protected final List<Term> rhs = new ArrayList<Term>();

	public Term getLhs() {
		return lhs;
	}

	public List<String> getOps() {
		return ops;
	}

	public int getOpsSize() {
		return ops.size();
	}

	public String getOp(final int index) {
		return ops.get(index);
	}

	public void addOp(final String s) {
		ops.add(s);
	}

	public List<Term> getRhs() {
		return rhs;
	}

	public int getRhsSize() {
		return rhs.size();
	}

	public Term getRhs(final int index) {
		return rhs.get(index);
	}

	public void addRhs(final Term t) {
		t.setParent(this);
		rhs.add(t);
	}

	public SimpleExpr (final Term lhs) {
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

	public SimpleExpr clone() {
		final SimpleExpr e = new SimpleExpr(lhs.clone());
		for (final String s : ops)
			e.addOp(s);
		for (final Term t : rhs)
			e.addRhs(t.clone());
		copyFieldsTo(e);
		return e;
	}
}
