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
package boa.types;

import boa.compiler.ast.types.AbstractType;
import boa.compiler.ast.types.VisitorType;
import boa.compiler.SymbolTable;

/**
 * A {@link BoaType} that represents a visitor.
 *
 * @author rdyer
 */
public class BoaVisitor extends BoaType {
	/**
	 * Construct a {@link BoaVisitor}.
	 */
	public BoaVisitor() {
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		if (!(that instanceof BoaVisitor))
			return false;

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public AbstractType toAST(final SymbolTable env) {
		final AbstractType t = new VisitorType();
		t.env = env;
		t.type = this;
		return t;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "visitor";
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.runtime.BoaAbstractVisitor";
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
		return true;
	}
}
