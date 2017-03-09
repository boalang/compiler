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

import boa.compiler.ast.Node;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class IfStatement extends Statement {
	protected Expression condition;
	protected Block body;
	protected Block elseBody;

	public Expression getCondition() {
		return condition;
	}

	public Block getBody() {
		return body;
	}

	public boolean hasElse() {
		return elseBody != null;
	}

	public Block getElse() {
		return elseBody;
	}

	public void setElse(final Statement elseBody) {
		setElse(Node.ensureBlock(elseBody));
	}

	public void setElse(final Block elseBody) {
		elseBody.setParent(this);
		this.elseBody = elseBody;
	}

	public IfStatement(final Expression condition, final Statement s) {
		this(condition, Node.ensureBlock(s));
	}

	public IfStatement(final Expression condition, final Block body) {
		this(condition, body, null);
	}

	public IfStatement(final Expression condition, final Block body, final Statement s2) {
		this(condition, body, Node.ensureBlock(s2));
	}

	public IfStatement(final Expression condition, final Statement s, final Block elseBody) {
		this(condition, Node.ensureBlock(s), elseBody);
	}

	public IfStatement(final Expression condition, final Statement s, final Statement s2) {
		this(condition, Node.ensureBlock(s), Node.ensureBlock(s2));
	}

	public IfStatement(final Expression condition, final Block body, final Block elseBody) {
		if (condition != null)
			condition.setParent(this);
		if (body != null)
			body.setParent(this);
		if (elseBody != null)
			elseBody.setParent(this);
		this.condition = condition;
		this.body = body;
		this.elseBody = elseBody;
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
		if (oldExp == condition) {
			newExp.setParent(this);
			condition = newExp;
		}
	}

	public IfStatement clone() {
		final IfStatement s;
		if (hasElse())
			s = new IfStatement(condition.clone(), body.clone(), elseBody.clone());
		else
			s = new IfStatement(condition.clone(), body.clone());
		copyFieldsTo(s);
		return s;
	}
}
