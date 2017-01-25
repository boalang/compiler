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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link BoaType} representing a data structure with named members of
 * arbitrary type.
 * 
 * @author anthonyu
 * @author rdyer
 */
public class BoaTuple extends BoaType {
	protected final List<BoaType> members;
	protected final Map<String, Integer> names;

	private static final Map<String, String> tupleNames = new HashMap<String, String>();

	public BoaTuple() {
		members = new ArrayList<BoaType>();
		names = new HashMap<String, Integer>();
	}

	public BoaTuple(final List<BoaType> members) {
		this.members = members;
		this.names = new HashMap<String, Integer>();
		for (int i = 0; i < this.members.size(); i++) {
			BoaType t = this.members.get(i);
			if (t instanceof BoaName)
				this.names.put(((BoaName) t).getId(), i);
		}
	}

	public BoaTuple(final List<BoaType> members, final Map<String, Integer> names) {
		this.members = members;
		this.names = names;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		if (that instanceof BoaArray) {
			BoaType type = ((BoaArray) that).getType();
			if (type instanceof BoaName)
				type = ((BoaName) type).getType();
			for (BoaType t : this.members)
				if (!t.assigns(type))
					return false;
			return true;
		}

		// FIXME have to construct it somehow
		//if (that instanceof BoaBytes)
		//	return true;

		if (!(that instanceof BoaTuple))
			return false;

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		return this.assigns(that);
	}

	/**
	 * 
	 * @param member
	 *            A {@link String} containing the name of the member
	 * 
	 * @return true if a member exists in this tuple with the given name
	 */
	public boolean hasMember(final String member) {
		return this.names.containsKey(member);
	}

	/**
	 * Return the type of the member identified by a given index.
	 * 
	 * @param index
	 *            An int containing the index of the member
	 * 
	 * @return A {@link BoaType} representing the type of the member
	 * 
	 */
	public BoaType getMember(final int index) {
		return this.members.get(index);
	}

	/**
	 * Return the type of the member identified by a given name.
	 * 
	 * @param member
	 *            A {@link String} containing the name of the member
	 * 
	 * @return A {@link BoaType} representing the type of the member
	 * 
	 */
	public BoaType getMember(final String member) {
		return this.members.get(this.names.get(member));
	}

	public int getMemberIndex(final String member) {
		return this.names.get(member);
	}

	public List<BoaType> getTypes() {
		return this.members;
	}

	@Override
	public String toJavaType() {
		String s = "";
		for (final BoaType t : this.members)
			s += "_" + cleanType(t.toJavaType());
		// use numbers for each unique type, to avoid generating really long filenames
		if (!tupleNames.containsKey(s))
			tupleNames.put(s, "BoaTup_" + tupleNames.size());

		return tupleNames.get(s);
	}

	private String cleanType(String s) {
		final String s2 = s.replace('<', '_').replace('>', '_').replaceAll(",\\s+", "_").replaceAll("\\[\\]", "Array");
		if (!s2.contains("."))
			return s2;
		return s2.substring(s2.lastIndexOf(".") + 1);
	}

	private int hash = 0;

	@Override
	public int hashCode() {
		if (hash == 0) {
			final int prime = 31;
			hash = super.hashCode();
			hash = prime * hash + (this.members == null ? 0 : this.members.hashCode());
		}
		return hash;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final BoaTuple other = (BoaTuple) obj;
		if (this.members == null) {
			if (other.members != null)
				return false;
		} else if (!this.members.equals(other.members))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "tuple " + this.members.toString();
	}
}
