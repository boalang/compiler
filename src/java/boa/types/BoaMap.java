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
 * A {@link BoaType} representing a mapping a set of keys to some value.
 * 
 * @author anthonyu
 */
public class BoaMap extends BoaType {
	private final BoaType valueType;
	private final BoaType indexType;

	/**
	 * Construct a BoaMap.
	 */
	public BoaMap() {
		this(null, null);
	}

	/**
	 * Construct a BoaMap.
	 * 
	 * @param valueType
	 *            A {@link BoaType} representing the valueType of the values in
	 *            this map
	 * 
	 * @param indexType
	 *            A {@link BoaType} representing the valueType of the indices in
	 *            this map
	 */
	public BoaMap(final BoaType valueType, final BoaType indexType) {
		this.valueType = valueType;
		this.indexType = indexType;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		// if that is a function, check the return value
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		// otherwise, if that is not a map, forget it
		if (!(that instanceof BoaMap))
			return false;

		// if that index valueType is not equivalent this this's, forget it
		if (!((BoaMap) that).indexType.assigns(this.indexType))
			return false;

		// same for the value valueType
		if (!((BoaMap) that).valueType.assigns(this.valueType))
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

		// otherwise, if that is not a map, forget it
		if (!(that instanceof BoaMap))
			return false;

		// if that index valueType is not equivalent this this's, forget it
		if (!this.indexType.accepts(((BoaMap) that).indexType))
			return false;

		// same for the value valueType
		if (!this.valueType.accepts(((BoaMap) that).valueType))
			return false;

		// ok
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasTypeVar() {
		return this.indexType.hasTypeVar() || this.valueType.hasTypeVar();
	}

	/**
	 * Get the valueType of the values of this map.
	 * 
	 * @return A {@link BoaType} representing the valueType of the values of this
	 *         map
	 */
	public BoaType getType() {
		return this.valueType;
	}

	/**
	 * Get the valueType of the indices of this map.
	 * 
	 * @return A {@link BoaType} representing the valueType of the indices of this
	 *         map
	 */
	public BoaType getIndexType() {
		return this.indexType;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "map[" + this.indexType + "] of " + this.valueType;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "java.util.HashMap<" + this.indexType.toBoxedJavaType() + ", " + this.valueType.toBoxedJavaType() + ">";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.indexType == null ? 0 : this.indexType.hashCode());
		result = prime * result + (this.valueType == null ? 0 : this.valueType.hashCode());
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
		final BoaMap other = (BoaMap) obj;
		if (this.indexType == null) {
			if (other.indexType != null)
				return false;
		} else if (!this.indexType.equals(other.indexType))
			return false;
		if (this.valueType == null) {
			if (other.valueType != null)
				return false;
		} else if (!this.valueType.equals(other.valueType))
			return false;
		return true;
	}
}
