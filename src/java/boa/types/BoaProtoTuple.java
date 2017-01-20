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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@link BoaTuple} representing a protocol buffer tuple.
 * 
 * @author rdyer
 */
public class BoaProtoTuple extends BoaTuple {
	public BoaProtoTuple(final List<BoaType> members, final Map<String, Integer> names) {
		super(members, names);
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		if (!super.assigns(that))
			return false;

		return this.getClass() == that.getClass();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (this.getClass() != obj.getClass())
			return false;
		if (!super.equals(obj))
			return false;
//		final BoaProtoTuple other = (BoaProtoTuple) obj;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final String type = toJavaType();
		return type.substring(type.lastIndexOf('.') + 1);
	}

	/**
	 * The set of all types that may be visited when starting a
	 * visit from this type.
	 * 
	 * @return the set of reachable types
	 */
	@SuppressWarnings("unchecked")
	public Set<Class<? extends BoaProtoTuple>> reachableTypes() {
		if (reachableTypesCache != null)
			return reachableTypesCache;

		reachableTypesCache = new HashSet<Class<? extends BoaProtoTuple>>();
		reachableTypesCache.add((Class<? extends BoaProtoTuple>) this.getClass());
		for (final BoaType t : members)
			if (t instanceof BoaProtoTuple) {
				reachableTypesCache.add((Class<? extends BoaProtoTuple>) t.getClass());
				reachableTypesCache.addAll(((BoaProtoTuple) t).reachableTypes());
			} else if (t instanceof BoaProtoList) {
				reachableTypesCache.addAll(((BoaProtoList) t).reachableTypes());
			}

		return reachableTypesCache;
	}

	protected Set<Class<? extends BoaProtoTuple>> reachableTypesCache;
}
