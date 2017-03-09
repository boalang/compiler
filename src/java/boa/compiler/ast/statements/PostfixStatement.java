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

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

import boa.compiler.ast.Node;
/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class PostfixStatement extends Statement {
	protected Expression e;
	protected String op;

	public Expression getExpr() {
		return e;
	}

	public String getOp() {
		return op;
	}

	public PostfixStatement (final Expression e, final String op) {
		if (e != null)
			e.setParent(this);
		this.e = e;
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

	@Override
	public void replaceExpression(final Expression oldExp, final Expression newExp) {
		if (oldExp == e) {
			newExp.setParent(this);
			e = newExp;
		}
	}

	public PostfixStatement clone() {
		final PostfixStatement s = new PostfixStatement(e.clone(), op);
		copyFieldsTo(s);
		return s;
	}
}
