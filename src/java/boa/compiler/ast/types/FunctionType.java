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
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class FunctionType extends AbstractType {
	protected final List<Component> args = new ArrayList<Component>();
	protected AbstractType t;

	public List<Component> getArgs() {
		return args;
	}

	public int getArgsSize() {
		return args.size();
	}

	public Component getArg(final int index) {
		return args.get(index);
	}

	public void addArg(final Component c) {
		c.setParent(this);
		args.add(c);
	}

	public boolean hasType() {
		return t != null;
	}

	public AbstractType getType() {
		return t;
	}

	public void setType(final AbstractType t) {
		t.setParent(this);
		this.t = t;
	}

	public FunctionType () {
	}

	public FunctionType (final AbstractType t) {
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
	public void accept(final AbstractVisitorNoArg v) {
		v.visit(this);
	}

	public FunctionType clone() {
		final FunctionType f;
		if (hasType())
			f = new FunctionType(t.clone());
		else
			f = new FunctionType();
		for (final Component c : args)
			f.addArg(c.clone());
		copyFieldsTo(f);
		return f;
	}
}
