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

import boa.compiler.ast.types.AbstractType;
import boa.compiler.ast.Identifier;
import boa.compiler.SymbolTable;

/**
 * A {@link BoaScalar} representing a time value in milliseconds since 1970.
 *
 * @author anthonyu
 * @author rdyer
 */
public class BoaTime extends BoaScalar {
	/** {@inheritDoc} */
	@Override
	public BoaScalar arithmetics(final BoaType that) {
		// if that is a function, try its return type
		if (that instanceof BoaFunction)
			return this.arithmetics(((BoaFunction) that).getType());
		// otherwise, if it is a time or in, the type is time
		else if (that instanceof BoaTime || that instanceof BoaInt)
			return new BoaTime();

		return super.arithmetics(that);
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		return this.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public AbstractType toAST(final SymbolTable env) {
		final AbstractType t = new Identifier("time");
		t.env = env;
		return t;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "time";
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

	/** {@inheritDoc} */
	@Override
	public String defaultValue() {
		return "0L";
	}
}
