package boa.types;

/**
 * A {@link BoaType} representing an array of scalar values that is a BoaProtoTuple member.
 * 
 * @author rdyer
 * 
 */
public class BoaProtoList extends BoaType {
	private BoaType type;

	/**
	 * Construct a BoaProtoList.
	 */
	public BoaProtoList() {
	}

	/**
	 * Construct a BoaProtoList.
	 * 
	 * @param boaType
	 *            A {@link BoaType} representing the type of the elements in
	 *            this array
	 */
	public BoaProtoList(final BoaType boaType) {
		this.type = boaType;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		// if that is a function, check its return type
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		if (that instanceof BoaTuple) {
			for (BoaType t : ((BoaTuple) that).getTypes())
				if (!this.type.assigns(t))
					return false;
			return true;
		}

		// otherwise, if it's not an array, forget it
		if (!(that instanceof BoaProtoList))
			return false;

		// if the element types are wrong, forget it
		if (this.type.assigns(((BoaProtoList) that).type))
			return true;

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		// if that is a function, check its return type
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		// otherwise, if it's not an array, forget it
		if (!(that instanceof BoaProtoList))
			return false;

		// if the element types are wrong, forget it
		if (this.type.accepts(((BoaProtoList) that).type))
			return true;

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean compares(final BoaType that) {
		// FIXME: is this needed?
		// if that is an array..
		if (that instanceof BoaProtoList)
			// check against the element types of these arrays
			return this.type.compares(((BoaProtoList) that).type);

		// otherwise, forget it
		return false;
	}

	/**
	 * Get the element type of this array.
	 * 
	 * @return A {@link BoaScalar} representing the element type of this
	 *         array
	 */
	public BoaScalar getType() {
		return (BoaScalar) this.type;
	}

	/**
	 * Set the element type of this array.
	 * 
	 * @param type
	 *            A {@link BoaScalar} representing the element type of this
	 *            array
	 */
	public void setType(final BoaScalar type) {
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

		final BoaProtoList other = (BoaProtoList) obj;

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
		return this.type.toJavaType() + "[]";
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
