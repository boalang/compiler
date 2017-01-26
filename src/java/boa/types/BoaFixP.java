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

/**
 * A {@link BoaFixP} that represents a fixpoint.
 * 
 * @author rramu
 */
public class BoaFixP extends BoaType {
	/**
	 * Construct a {@link BoaFixP}.
	 */
	public BoaFixP() {
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		if (!(that instanceof BoaFixP))
			return false;

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "fixp";
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.runtime.BoaAbstractFixP";
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
