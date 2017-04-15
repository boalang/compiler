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
 * Base class for the types in Boa.
 * 
 * @author anthonyu
 */
public abstract class BoaType {
	/**
	 * Returns the type that results from an expression of this type and an
	 * expression of that type in an arithmetic expression. (e.g. an int plus a
	 * float results in an expression of type float).
	 * 
	 * @param that
	 *            A BoaType representing the other expression's type
	 * 
	 * @return A BoaType representing the type of the resulting expression
	 */
	public BoaScalar arithmetics(final BoaType that) {
		// by default, no types are allowed in arithmetic
		throw new RuntimeException("incorrect type for arithmetic, found: " + that + ", expected: " + this);
	}

	/**
	 * Returns true when an expression of that type may be assigned to a
	 * variable of this type.
	 * 
	 * @return A boolean representing whether an expression of that type may be
	 *         assigned to a variable of this type
	 */
	public boolean assigns(final BoaType that) {
		// by default no types can be assigned
		return false;
	}

	/**
	 * Returns true when an expression of that type may be used as a formal
	 * parameter of this type.
	 * 
	 * @return A boolean representing whether an expression of that type may be
	 *         used as a formal parameter of this type
	 */
	public boolean accepts(final BoaType that) {
		// by default no types will be accepted
		return false;
	}

	/**
	 * Returns true when an expression of that type may be compared to an
	 * expression of this type.
	 * 
	 * @return A boolean representing whether an expression of that type may be
	 *         compared to an expression of this type
	 */
	public boolean compares(final BoaType that) {
		// by default, no types can be compared
		return false;
	}

	/**
	 * Returns if this type contains a {@link BoaTypeVar} somewhere in it.
	 *
	 * @return true if this type has a {@link BoaTypeVar} in it
	 */
	public boolean hasTypeVar() {
		return false;
	}

	/**
	 * Returns a string representation of the Java equivalent of this Boa
	 * type.
	 * 
	 * @return A String containing the name of the Java type equivalent to this
	 *         Boa type
	 */
	public String toJavaType() {
		throw new RuntimeException("no java equivalent for type " + this.toString());
	}

	/**
	 * Returns a string representation of the boxed Java equivalent of this Boa
	 * type.
	 * 
	 * @return A String containing the name of the boxed Java type equivalent to this
	 *         Boa type
	 */
	public String toBoxedJavaType() {
		return toJavaType();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object that) {
		if (that == null)
			return false;

		// return whether the class names are the same
		return that.getClass().equals(this.getClass());
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public abstract String toString();
}
