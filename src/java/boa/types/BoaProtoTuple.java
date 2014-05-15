package boa.types;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link BoaTuple} representing a protocol buffer tuple.
 * 
 * @author rdyer
 * 
 */
public class BoaProtoTuple extends BoaTuple {
	/**
	 * Construct a BoaProtoTuple.
	 * 
	 * @param members
	 *            A {@link LinkedHashMap} of {@link BoaType} containing a
	 *            mapping from the names to the types of the members of this
	 *            tuple
	 * 
	 */
	public BoaProtoTuple(final List<BoaType> members) {
		this(members, new HashMap<String, Integer>());
	}

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
}
