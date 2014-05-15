package sizzle.types;

/**
 * A {@link SizzleType} representing an array of scalar values that is a SizzleProtoTuple member.
 * 
 * @author rdyer
 * 
 */
public class SizzleProtoList extends SizzleType {
	private SizzleType type;

	/**
	 * Construct a SizzleProtoList.
	 */
	public SizzleProtoList() {
	}

	/**
	 * Construct a SizzleProtoList.
	 * 
	 * @param sizzleType
	 *            A {@link SizzleType} representing the type of the elements in
	 *            this array
	 */
	public SizzleProtoList(final SizzleType sizzleType) {
		this.type = sizzleType;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final SizzleType that) {
		// if that is a function, check its return type
		if (that instanceof SizzleFunction)
			return this.assigns(((SizzleFunction) that).getType());

		if (that instanceof SizzleTuple) {
			for (SizzleType t : ((SizzleTuple) that).getTypes())
				if (!this.type.assigns(t))
					return false;
			return true;
		}

		// otherwise, if it's not an array, forget it
		if (!(that instanceof SizzleProtoList))
			return false;

		// if the element types are wrong, forget it
		if (this.type.assigns(((SizzleProtoList) that).type))
			return true;

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final SizzleType that) {
		// if that is a function, check its return type
		if (that instanceof SizzleFunction)
			return this.assigns(((SizzleFunction) that).getType());

		// otherwise, if it's not an array, forget it
		if (!(that instanceof SizzleProtoList))
			return false;

		// if the element types are wrong, forget it
		if (this.type.accepts(((SizzleProtoList) that).type))
			return true;

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean compares(final SizzleType that) {
		// FIXME: is this needed?
		// if that is an array..
		if (that instanceof SizzleProtoList)
			// check against the element types of these arrays
			return this.type.compares(((SizzleProtoList) that).type);

		// otherwise, forget it
		return false;
	}

	/**
	 * Get the element type of this array.
	 * 
	 * @return A {@link SizzleScalar} representing the element type of this
	 *         array
	 */
	public SizzleScalar getType() {
		if (this.type instanceof SizzleScalar)
			return (SizzleScalar) this.type;

		throw new RuntimeException("this shouldn't happen");
	}

	/**
	 * Set the element type of this array.
	 * 
	 * @param type
	 *            A {@link SizzleScalar} representing the element type of this
	 *            array
	 */
	public void setType(final SizzleScalar type) {
		this.type = type;
	}

	private int hash = 0;

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		if (hash == 0) {
			final int prime = 31;
			hash = 1;
			hash = prime * hash + (this.type == null ? 0 : this.type.hashCode());
		}
		return hash;
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

		final SizzleProtoList other = (SizzleProtoList) obj;

		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "java.util.List<" + this.type.toJavaType() + ">";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (this.type == null)
			return "protolist of none";
		else
			return "protolist of " + this.type.toString();
	}
}
