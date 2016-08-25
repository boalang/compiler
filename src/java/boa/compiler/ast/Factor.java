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

import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class Factor extends Node {
	protected Operand op;
	protected final List<Node> ops = new ArrayList<Node>();

	public Operand getOperand() {
		return op;
	}

	public List<Node> getOps() {
		return ops;
	}

	public int getOpsSize() {
		return ops.size();
	}

	public Node getOp(final int index) {
		return ops.get(index);
	}

	public Factor addOp(final Node op) {
		op.setParent(this);
		ops.add(op);
		return this;
	}

	public Factor (final Operand op) {
		if (op != null)
			op.setParent(this);
		this.op = op;
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

	public Factor clone() {
		final Factor f = new Factor(op.clone());
		for (final Node n : ops)
			f.addOp(n.clone());
		copyFieldsTo(f);
		return f;
	}
}
