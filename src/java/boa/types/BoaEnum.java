/*
 * Copyright 2017, Anthony Urso, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology
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

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A {@link BoaScalar} representing a data structure with named members of
 * enum type.
 *
 * @author ankuraga
 * @author rdyer
 */
public class BoaEnum extends BoaScalar {
	protected final List<BoaEnum> members;
	protected final Map<String, Integer> names;
	protected final List<String> values;
	private BoaType fieldType = null;
	private BoaEnum parent = null;

	/**
	 * Construct a BoaEnum.
	 *
	 * @param members
	 *            A {@link List} of {@link BoaEnum} representing the members of BoaEnum in
	 *            this enum
	 *
	 * @param names
	 *            A {@link Map} representing the names of the BoaEnum in this enum
	 *
	 * @param values
	 * 		      A {@link List} representing the values of each member in this enum
	 *
	 * @param fieldType
	 *  		  A {@link BoaType} representing the infer value type of a member in this enum
	 */
	public BoaEnum(final List<BoaEnum> members, final List<String> names, final List<String> values, final BoaType fieldType) {
		this.members = members;
		this.names = new HashMap<String, Integer>();
		for (int i = 0; i < this.members.size(); i++) {
			members.get(i).parent = this;
			this.names.put(names.get(i), i);
		}
		this.values = values;
		this.fieldType = fieldType;
	}

	/**
	 * Construct a BoaEnum.
	 *
	 * @param name
	 *            A {@link String} representing the name of the member
	 *
	 * @param value
	 * 		      A {@link String} representing the value of the member
	 *
	 * @param fieldType
	 * 			  A {@link BoaType} representing the infer value type of a member
	 */
	public BoaEnum(final String name, final String value, final BoaType fieldType) {
		this.members = new ArrayList<BoaEnum>();
		this.names = new HashMap<String, Integer>();
		this.names.put(name, 0);
		this.values = new ArrayList<String>();
		this.values.add(value);
		this.fieldType = fieldType;
	}

	/**
	 * Test to see if this enum contains a specific member.
	 *
	 * @param member
	 *            A {@link String} containing the name of the member
	 *
	 * @return true if a member exists in this enum with the given name
	 */
	public boolean hasMember(final String member) {
		return this.names.containsKey(member);
	}

	/**
	 * Return the type of the member identified by a given name.
	 *
	 * @param member
	 *            A {@link String} containing the name of the member
	 *
	 * @return A {@link BoaEnum} representing the type of the member
	 */
	public BoaEnum getMember(final String member) {
		return this.members.get(this.names.get(member));
	}

	/**
	 * Return the infer type of the field.
	 *
	 * @return A {@link BoaType} representing the infer type of the field
	 */
	public BoaType getType() {
		return this.fieldType;
	}

	@Override
	public String toJavaType() {
		if (this.parent != null)
			return this.parent.toJavaType();

		String s = "";

		for (final String name : this.names.keySet())
			s += name + this.values.get(this.names.get(name)) + "_";

		return shortenedType(s, "BoaEnum");
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "enum" + this.values;
	}
}
