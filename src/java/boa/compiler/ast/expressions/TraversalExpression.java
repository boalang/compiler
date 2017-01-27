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

import boa.compiler.ast.Operand;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.types.TraversalType;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rramu
 */
public class TraversalExpression extends Operand {
	protected TraversalType t;
	protected Block body;

	public TraversalType getType() {
		return t;
	}

	public Block getBody() {
		return body;
	}

	public TraversalExpression (final TraversalType t, final Block body) {
		if (t != null)
			t.setParent(this);
		if (body != null)
			body.setParent(this);
		this.t = t;
		this.body = body;
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

	public TraversalExpression clone() {
		final TraversalExpression e = new TraversalExpression(t.clone(), body.clone());
		copyFieldsTo(e);
		return e;
	}
}
