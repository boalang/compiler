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
package boa.compiler.ast.types;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Component;
import boa.compiler.ast.Identifier;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author ankuraga
 */
public class ModelType extends AbstractType {
	protected Identifier id;
	protected Component t;

	public Identifier getId() {
		return id;
	}

	public void setId(final Identifier id) {
		id.setParent(this);
		this.id = id;
	}

	public Component getType() {
		return t;
	}

	public void setType(final Component t) {
		t.setParent(this);
		this.t = t;
	}

	public ModelType (final Identifier id) {
		this(id, null);
	}

	public ModelType (final Identifier id, final Component t) {
		System.out.println(id);
		if (id != null)
			id.setParent(this);
		if (t != null)
			t.setParent(this);
		this.id = id;
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

	public ModelType clone() {
		final ModelType m;
		m = new ModelType(id.clone(), t.clone());
		copyFieldsTo(m);
		return m;
	}
}
