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

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class Index extends Node {
	protected Expression start;
	protected Expression end;

	public Expression getStart() {
		return start;
	}

	public void setStart(final Expression start) {
		start.setParent(this);
		this.start = start;
	}

	public boolean hasEnd() {
		return end != null;
	}

	public Expression getEnd() {
		return end;
	}

	public void setEnd(final Expression end) {
		end.setParent(this);
		this.end = end;
	}

	public Index (final Expression start) {
		this(start, null);
	}

	public Index (final Expression start, final Expression end) {
		if (start != null)
			start.setParent(this);
		if (end != null)
			end.setParent(this);
		this.start = start;
		this.end = end;
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

	public Index clone() {
		final Index i;
		if (hasEnd())
			i = new Index(start.clone(), end.clone());
		else
			i = new Index(start.clone());
		copyFieldsTo(i);
		return i;
	}
}
