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

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class ReturnStatement extends Statement {
	protected Expression expr;

	public boolean hasExpr() {
		return expr != null;
	}

	public Expression getExpr() {
		return expr;
	}

	public void setExpr(final Expression expr) {
		expr.setParent(this);
		this.expr = expr;
	}

	public ReturnStatement() {
		this(null);
	}

	public ReturnStatement(final Expression expr) {
		if (expr != null)
			expr.setParent(this);
		this.expr = expr;
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

	public ReturnStatement clone() {
		final ReturnStatement s;
		if (hasExpr())
			s = new ReturnStatement(expr.clone());
		else
			s = new ReturnStatement();
		copyFieldsTo(s);
		return s;
	}
}
