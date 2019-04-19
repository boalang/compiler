/*
 * Copyright 2019, Yijia Huang, Hridesh Rajan, 
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
package boa.types;

/**
 * A {@link BoaType} representing a queue of values.
 * 
 * @author hyj
 */
public class BoaQueue extends BoaType {
	private final BoaType type;

	/**
	 * Construct a {@link BoaQueue}.
	 */
	public BoaQueue() {
		this(null);
	}

	/**
	 * Construct a {@link BoaQueue}.
	 * 
	 * @param boaType
	 *            A {@link BoaType} representing the type of the values in
	 *            this queue
	 */
	public BoaQueue(final BoaType boaType) {
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

		// otherwise, if that is not a queue, forget it
		if (!(that instanceof BoaQueue))
			return false;

		// same for the value type
		if (!((BoaQueue) that).type.assigns(this.type))
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

		// otherwise, if that is not a queue, forget it
		if (!(that instanceof BoaQueue))
			return false;

		// same for the value type
		if (!this.type.accepts(((BoaQueue) that).type))
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
	 * Get the type of the values of this queue.
	 * 
	 * @return A {@link BoaType} representing the type of the values of this
	 *         queue
	 */
	public BoaType getType() {
		return this.type;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "queue of " + this.type;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "java.util.LinkedList<" + this.type.toBoxedJavaType() + ">";
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
		final BoaQueue other = (BoaQueue) obj;
		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;
		return true;
	}
}
