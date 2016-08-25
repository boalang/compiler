/*
 * Copyright 2014, Anthony Urso, Hridesh Rajan, Robert Dyer, 
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
 * A {@link BoaType} representing an array of scalar values.
 * 
 * @author anthonyu
 */
public class BoaArray extends BoaType {
	private BoaType type;

	/**
	 * Construct a BoaArray.
	 */
	public BoaArray() {
	}

	/**
	 * Construct a BoaArray.
	 * 
	 * @param boaType
	 *            A {@link BoaType} representing the type of the elements in
	 *            this array
	 */
	public BoaArray(final BoaType boaType) {
		this.type = boaType;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		// if that is a function, check its return type
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		if (that instanceof BoaTuple) {
			for (BoaType t : ((BoaTuple) that).getTypes())
				if (!this.type.assigns(t))
					return false;
			return true;
		}

		// otherwise, if it's not an array, forget it
		if (!(that instanceof BoaArray))
			return false;

		// if the element types are wrong, forget it
		if (this.type.assigns(((BoaArray) that).type))
			return true;

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		// if that is a function, check its return type
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		// otherwise, if it's not an array, forget it
		if (!(that instanceof BoaArray))
			return false;

		// if the element types are wrong, forget it
		if (this.type.accepts(((BoaArray) that).type))
			return true;

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean compares(final BoaType that) {
		// if that is an array..
		if (that instanceof BoaArray)
			// check against the element types of these arrays
			return this.type.compares(((BoaArray) that).type);

		// otherwise, forget it
		return false;
	}

	/**
	 * Get the element type of this array.
	 * 
	 * @return A {@link BoaScalar} representing the element type of this
	 *         array
	 */
	public BoaScalar getType() {
		if (this.type instanceof BoaScalar)
			return (BoaScalar) this.type;

		throw new RuntimeException("this shouldn't happen");
	}

	/**
	 * Set the element type of this array.
	 * 
	 * @param type
	 *            A {@link BoaScalar} representing the element type of this
	 *            array
	 */
	public void setType(final BoaScalar type) {
		this.type = type;
	}

	private int hash = 0;

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		if (hash == 0) {
			final int prime = 31;
			hash = 1;
			hash = prime * hash + (this.type == null ? 0 : this.type.hashCode());
		}
		return hash;
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

		final BoaArray other = (BoaArray) obj;

		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return this.type.toJavaType() + "[]";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (this.type == null)
			return "array of none";
		return "array of " + this.type.toString();
	}
}
