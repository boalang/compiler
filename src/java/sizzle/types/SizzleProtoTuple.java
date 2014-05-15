package sizzle.types;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link SizzleTuple} representing a protocol buffer tuple.
 * 
 * @author rdyer
 * 
 */
public class SizzleProtoTuple extends SizzleTuple {
	/**
	 * Construct a SizzleProtoTuple.
	 * 
	 * @param members
	 *            A {@link LinkedHashMap} of {@link SizzleType} containing a
	 *            mapping from the names to the types of the members of this
	 *            tuple
	 * 
	 */
	public SizzleProtoTuple(final List<SizzleType> members) {
		this(members, new HashMap<String, Integer>());
	}

	public SizzleProtoTuple(final List<SizzleType> members, final Map<String, Integer> names) {
		super(members, names);
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final SizzleType that) {
		if (!super.assigns(that))
			return false;

		return (that instanceof SizzleProtoTuple);
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
//		final SizzleProtoTuple other = (SizzleProtoTuple) obj;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String type = toJavaType();
		return type.substring(1 + type.lastIndexOf('.'));
	}
}
