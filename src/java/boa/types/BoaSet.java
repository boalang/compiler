/*
 * Copyright 2014-2021, Anthony Urso, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology
 *                 and University of Nebraska Board of Regents
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
package boa.types;

import boa.compiler.ast.Component;
import boa.compiler.ast.types.AbstractType;
import boa.compiler.ast.types.SetType;
import boa.compiler.SymbolTable;

/**
 * A {@link BoaType} representing a set of values.
 *
 * @author rdyer
 */
public class BoaSet extends BoaType {
	private final BoaType type;

	/**
	 * Construct a {@link BoaSet}.
	 */
	public BoaSet() {
		this(null);
	}

	/**
	 * Construct a {@link BoaSet}.
	 *
	 * @param boaType
	 *            A {@link BoaType} representing the type of the values in
	 *            this set
	 */
	public BoaSet(final BoaType boaType) {
		this.type = boaType;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		// if that is a function, check the return type
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		// if that is a component, check the type
		if (that instanceof BoaName)
			return this.assigns(((BoaName) that).getType());

		// otherwise, if that is not a set, forget it
		if (!(that instanceof BoaSet))
			return false;

		// same for the value type
		if (!((BoaSet) that).type.assigns(this.type))
			return false;

		// ok
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		// if that is a function, check the return value
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		// otherwise, if that is not a set, forget it
		if (!(that instanceof BoaSet))
			return false;

		// same for the value type
		if (!this.type.accepts(((BoaSet) that).type))
			return false;

		// ok
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean compares(final BoaType that) {
		// if that is a function, check the return type
		if (that instanceof BoaFunction)
			return this.compares(((BoaFunction) that).getType());

		// otherwise, check if the types are equivalent one way or the other
		if (this.assigns(that) || that.assigns(this))
			return true;

		// forget it
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasTypeVar() {
		return this.type.hasTypeVar();
	}

	/**
	 * Get the type of the values of this set.
	 *
	 * @return A {@link BoaType} representing the type of the values of this
	 *         set
	 */
	public BoaType getType() {
		return this.type;
	}

	/** {@inheritDoc} */
	@Override
	public AbstractType toAST(final SymbolTable env) {
		final Component c = new Component(this.type.toAST(env));
		final AbstractType t = new SetType(c);
		c.env = t.env = env;
		t.type = this;
		return t;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "set of " + this.type;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "java.util.LinkedHashSet<" + this.type.toInterfaceJavaType() + ">";
	}

	/** {@inheritDoc} */
	@Override
	public String toInterfaceJavaType() {
		return "java.util.Set<" + this.type.toInterfaceJavaType() + ">";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.type == null ? 0 : this.type.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final BoaSet other = (BoaSet) obj;
		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;
		return true;
	}
}
