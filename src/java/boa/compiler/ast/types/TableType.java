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
package boa.compiler.ast.types;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Component;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hungc
 */
public class TableType extends AbstractType {
	protected final List<Component> indices = new ArrayList<Component>();
	protected Component t;

	public List<Component> getIndices() {
		return indices;
	}

	public int getIndicesSize() {
		return indices.size();
	}

	public Component getIndice(final int index) {
		return indices.get(index);
	}

	public void addIndice(final Component c) {
		c.setParent(this);
		indices.add(c);
	}

	public Component getType() {
		return t;
	}

	public void setType(final Component t) {
		t.setParent(this);
		this.t = t;
	}

	public TableType () {
		this(null);
	}

	public TableType (final Component t) {
		if (t != null)
			t.setParent(this);
		this.t = t;
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

	public TableType clone() {
		final TableType tableType = new TableType(t.clone());
		for (final Component c : indices)
			tableType.addIndice(c.clone());
		copyFieldsTo(tableType);
		return tableType;
	}
}
