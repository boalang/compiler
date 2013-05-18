package boa.types;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@link BoaTuple} representing a protocol buffer tuple.
 * 
 * @author rdyer
 * 
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
		String type = toJavaType();
		return type.substring(1 + type.lastIndexOf('.'));
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
