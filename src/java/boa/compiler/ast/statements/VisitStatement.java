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

import boa.compiler.ast.Component;
import boa.compiler.ast.Identifier;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class VisitStatement extends Statement {
	protected boolean before;
	protected boolean wildcard = false;
	protected Component node;
	protected final List<Identifier> ids = new ArrayList<Identifier>();
	protected Block body;

	public boolean isBefore() {
		return before;
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

	public VisitStatement(final boolean before) {
		this.before = before;
	}

	public VisitStatement(final boolean before, final boolean wildcard, final Block body) {
		this(before, null, body);
		this.wildcard = wildcard;
	}

	public VisitStatement(final boolean before, final Component node, final Block body) {
		if (node != null)
			node.setParent(this);
		if (body != null)
			body.setParent(this);
		this.before = before;
		this.node = node;
		this.body = body;
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

	public VisitStatement clone() {
		final VisitStatement v = new VisitStatement(before, wildcard, body.clone());
		if (hasComponent())
			v.node = node.clone();
		for (final Identifier id : ids)
			v.addId(id.clone());
		copyFieldsTo(v);
		return v;
	}
}
