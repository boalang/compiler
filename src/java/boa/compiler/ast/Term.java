/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, Che Shian Hung
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

import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
 * @author hungc
 */
public class Term extends Node {
	protected Factor lhs;
	protected final List<String> ops = new ArrayList<String>();
	protected final List<Factor> rhs = new ArrayList<Factor>();

	public Factor getLhs() {
		return lhs;
	}

	public void setLhs(final Factor f) {
		f.setParent(this);
		this.lhs = f;
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

	public void addOpFront(final String s) {
		ops.add(0, s);
	}

	public List<Factor> getRhs() {
		return rhs;
	}

	public int getRhsSize() {
		return rhs.size();
	}

	public Factor getRhs(final int index) {
		return rhs.get(index);
	}

	public void addRhs(final Factor f) {
		f.setParent(this);
		rhs.add(f);
	}

	public void addRhsFront(final Factor f) {
		f.setParent(this);
		rhs.add(0, f);
	}

	public Term (final Factor lhs) {
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
	public void accept(final AbstractVisitorNoArgNoRet v) {
		v.visit(this);
	}

	public Term clone() {
		final Term t = new Term(lhs.clone());
		for (final String s : ops)
			t.addOp(s);
		for (final Factor f : rhs)
			t.addRhs(f.clone());
		copyFieldsTo(t);
		return t;
	}
}
