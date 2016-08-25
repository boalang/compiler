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
 * A {@link BoaScalar} representing a 64 bit integer value.
 * 
 * @author anthonyu
 */
public class BoaInt extends BoaScalar {
	/** {@inheritDoc} */
	@Override
	public BoaScalar arithmetics(final BoaType that) {
		// if that is a function, check its return value
		if (that instanceof BoaFunction)
			return this.arithmetics(((BoaFunction) that).getType());
		// otherwise, if it is an int, the type is int
		else if (that instanceof BoaInt)
			return new BoaInt();
		// otherwise, if it's a time, the type is time
		else if (that instanceof BoaTime)
			return new BoaTime();
		// otherwise if it's a float, the type is float
		else if (that instanceof BoaFloat)
			return new BoaFloat();

		// otherwise, check the default
		return super.arithmetics(that);
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		return this.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "int";
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "long";
	}

	/** {@inheritDoc} */
	@Override
	public String toBoxedJavaType() {
		return "Long";
	}
}
