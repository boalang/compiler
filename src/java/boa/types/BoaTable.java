/*
 * Copyright 2018, Robert Dyer, Che Shian Hung,
 *                 and Bowling Green State University
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

import java.util.List;

/**
 * A {@link BoaType} representing an aggregator that can be emitted to.
 * 
 * @author rdyer
 * @author hungc
 */
public class BoaTable extends BoaType {
	private BoaType type;
	private List<BoaScalar> indexTypes;

	/**
	 * Construct a BoaTable.
	 * 
	 * @param types
	 *            A {@link List} of {@link BoaType} representing the types of
	 *            this BoaTable
	 */
	public BoaTable(final BoaType type) {
		this(type, null);
	}

	/**
	 * Construct a BoaTable.
	 * 
	 * @param type
	 *            A {@link BoaType} representing the type of this BoaTable
	 * 
	 * @param indexTypes
	 *            A {@link List} of {@link BoaScalar} representing the index
	 *            types of this BoaTable
	 */
	public BoaTable(final BoaType type, final List<BoaScalar> indexTypes) {
		this.type = type;
		this.indexTypes = indexTypes;
	}

	/**
	 * Return the number of indices this table has.
	 * 
	 * @return An int containing the number of types each emit to this table
	 *         will require
	 */
	public int countIndices() {
		if (this.indexTypes == null)
			return 0;
		return this.indexTypes.size();
	}

	/**
	 * Get the type of value to be emitted to this table.
	 * 
	 * @return A {@link BoaType} representing the type of the value to be
	 *         emitted to this table
	 * 
	 */
	public BoaType getType() {
		return this.type;
	}

	/**
	 * Get the type of the index at that position.
	 * 
	 * @param position
	 *            An int representing the position
	 * 
	 * @return A {@link BoaScalar} representing the type of the index at that
	 *         position
	 * 
	 */
	public BoaScalar getIndex(final int position) {
		return this.indexTypes.get(position);
	}

	/**
	 * Returns whether this table will accept an emit of those types.
	 * 
	 * @param types
	 *            An {@link List} of {@link BoaType} containing the types to
	 *            be emitted
	 * 
	 * @return True if this table will accept them, false otherwise
	 */
	@Override
	public boolean accepts(final BoaType type) {
		// check if the types are equivalent
		if (!this.type.assigns(type))
			return false;

		// they were
		return true;
	}

	/**
	 * Set the type of the values to be emitted to this table.
	 * 
	 * @param types
	 *            A {@link BoaType} representing the type of the values to be
	 *            emitted to this table
	 * 
	 */
	public void setType(final BoaType type) {
		this.type = type;
	}

	/**
	 * Get the types of the indices into this table.
	 * 
	 * @return A {@link List} of {@link BoaScalar} representing the types of
	 *         the indices into this table
	 * 
	 */
	public List<BoaScalar> getIndexTypes() {
		return this.indexTypes;
	}

	/**
	 * Set the types of the indices into this table.
	 * 
	 * @param indexTypes
	 *            A {@link List} of {@link BoaScalar} representing the types
	 *            of the indices into this table
	 * 
	 */
	public void setIndexTypes(final List<BoaScalar> indexTypes) {
		this.indexTypes = indexTypes;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (this.indexTypes == null ? 0 : this.indexTypes.hashCode());
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
		final BoaTable other = (BoaTable) obj;
		if (this.indexTypes == null) {
			if (other.indexTypes != null)
				return false;
		} else if (!this.indexTypes.equals(other.indexTypes))
			return false;
		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String s = "table " + this.getType();
		if (this.indexTypes != null)
			s += this.indexTypes.toString();
		return s;
	}
}
