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

import java.util.ArrayList;
import java.util.List;
import boa.types.BoaTuple;

import boa.compiler.ast.Component;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;
import boa.compiler.ast.types.AbstractType;

import boa.compiler.ast.Node;
/**
 * 
 * @author rramu
 */
public class TraverseStatement extends Statement {
	protected boolean wildcard = false;
	protected Component node;
	protected final List<Identifier> ids = new ArrayList<Identifier>();
	protected Block body;
	protected Expression condition;
	protected List<IfStatement> ifStatements = new ArrayList<IfStatement>();
	public AbstractType returnType;

	public void addIfStatement(IfStatement ifStatement) {
		ifStatement.setParent(this);
		ifStatements.add(ifStatement);
	}
	
	public AbstractType getReturnType() {
		return returnType;
	}

	public List<IfStatement> getIfStatements() {
		return ifStatements;
	}

	public Expression getCondition() {
		return condition;
	}

	public void setCondition(final Expression condition) {
		condition.setParent(this);
		this.condition=condition;
	}

	public void setReturnType(final AbstractType returnType) {
		this.returnType=returnType;
	}

	public boolean hasWildcard() {
		return wildcard;
	}

	public void setWildcard(final boolean wildcard) {
		this.wildcard = wildcard;
	}

	public boolean hasComponent() {
		return node != null;
	}

	public boolean hasBody() {
		return body != null;
	}

	public boolean hasCondition() {
		return condition != null;
	}

	public Component getComponent() {
		return node;
	}

	public void setComponent(final Component node) {
		node.setParent(this);
		this.node = node;
	}

	public List<Identifier> getIdList() {
		return ids;
	}

	public int getIdListSize() {
		return ids.size();
	}

	public Identifier getId(int index) {
		return ids.get(index);
	}

	public void addId(final Identifier id) {
		id.setParent(this);
		ids.add(id);
	}

	public Block getBody() {
		return body;
	}

	public void setBody(final Statement s) {
		setBody(ensureBlock(s));
	}

	public void setBody(final Block body) {
		body.setParent(this);
		this.body = body;
	}

	public TraverseStatement() {
	}

	public TraverseStatement(final boolean wildcard) {
		this(null, null, null);
		this.wildcard = wildcard;
	}

	public TraverseStatement(final boolean wildcard, final Block body, final Expression condition) {
		this(null, body, condition);
		this.wildcard = wildcard;
	}

	public TraverseStatement(final Component node, final Block body, final Expression condition) {
		if (node != null)
			node.setParent(this);
		if (body != null)
			body.setParent(this);
		if (condition != null)
			condition.setParent(this);
		this.node = node;
		this.body = body;
		this.condition=condition;
	}

	/** {@inheritDoc} */
	@Override
	public <T,A> T accept(AbstractVisitor<T,A> v, A arg) {
		return v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public <A> void accept(AbstractVisitorNoReturn<A> v, A arg) {
		v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}

	@Override
	public void replaceExpression(final Expression oldExp, final Expression newExp) {
		if (oldExp == condition) {
			newExp.setParent(this);
			condition = newExp;
		}
	}

	public TraverseStatement clone() {
		final TraverseStatement v = new TraverseStatement(wildcard);
		if(hasBody()) 
			v.body=body.clone();
		if (hasComponent())
			v.node = node.clone();
		if (hasCondition())
			v.condition = condition.clone();
		if (returnType!=null)
			v.returnType = this.returnType;
		for (final Identifier id : ids)
			v.addId(id.clone());
		for (final IfStatement ifStatement : ifStatements)
			v.addIfStatement(ifStatement.clone());
		copyFieldsTo(v);
		return v;
	}
}
