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
 * A {@link BoaScalar} representing an double precision floating point value.
 * 
 * @author anthonyu
 */
public class BoaFloat extends BoaScalar {
	/** {@inheritDoc} */
	@Override
	public BoaScalar arithmetics(final BoaType that) {
		// if that is a function, check its return type
		if (that instanceof BoaFunction)
			return this.arithmetics(((BoaFunction) that).getType());

		// if it's a float, the type is float
		if (that instanceof BoaFloat)
			return new BoaFloat();

		// same with ints
		if (that instanceof BoaInt)
			return new BoaFloat();

		return super.arithmetics(that);
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		// ints can be assigned to floats
		if (that instanceof BoaInt)
			return true;

		// otherwise, just check the defaults
		return super.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		return this.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "double";
	}

	/** {@inheritDoc} */
	@Override
	public String toBoxedJavaType() {
		return "Double";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "float";
	}
}
