/*
 * Copyright 2018, Robert Dyer, Che Shian Hung, and Bowling Green State University
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

import java.util.List;
import java.util.ArrayList;

import boa.compiler.ast.Identifier;
import boa.compiler.ast.Program;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hungc
 */
public class SubView extends Statement {
	protected Identifier id;
	protected Program p;
	protected SubView parentView;
	protected List<SubView> childViews;

	public Identifier getId() {
		return id;
	}

	public Program getProgram() {
		return p;
	}

	public SubView getParentView() {
		return parentView;
	}

	public SubView getChildView(int i) {
		if (!this.hasChildViews() || childViews.size() <= i)
			return null;

		return childViews.get(i);
	}

	public List<SubView> getChildViews() {
		return childViews;
	}

	public boolean hasParentView() {
		return parentView != null;
	}

	public boolean hasChildViews() {
		return childViews != null && childViews.size() > 0;
	}

	public void setParentView(SubView sv) {
		parentView = sv;
	}

	public void addChildView(SubView sv) {
		if (childViews == null)
			childViews = new ArrayList<SubView>();

		childViews.add(sv);
	}

	public SubView (final Identifier id, final Program p) {
		parentView = null;
		childViews = null;

		if (id != null)
			id.setParent(this);
		if (p != null)
			p.setParent(this);
		this.id = id;
		this.p = p;
		p.jobName = id.getToken();
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

	public SubView clone() {
		final SubView sv = new SubView(id.clone(), p.clone());
		copyFieldsTo(sv);
		return sv;
	}
}
