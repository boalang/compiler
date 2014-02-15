package boa.types;

/**
 * A {@link BoaType} representing a set of values.
 * 
 * @author rdyer
 */
public class BoaSet extends BoaType {
	private final BoaType type;

	/**
	 * Construct a {@link BoaSet}.
	 */
	public BoaSet() {
		this(null);
	}

	/**
	 * Construct a {@link BoaSet}.
	 * 
	 * @param boaType
	 *            A {@link BoaType} representing the type of the values in
	 *            this set
	 */
	public BoaSet(final BoaType boaType) {
		this.type = boaType;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		// if that is a function, check the return value
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		// otherwise, if that is not a set, forget it
		if (!(that instanceof BoaSet))
			return false;

		// same for the value type
		if (!((BoaSet) that).type.assigns(this.type))
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

		// otherwise, if that is not a set, forget it
		if (!(that instanceof BoaSet))
			return false;

		// same for the value type
		if (!this.type.accepts(((BoaSet) that).type))
			return false;

		// ok
		return true;
	}

	/**
	 * Get the type of the values of this set.
	 * 
	 * @return A {@link BoaType} representing the type of the values of this
	 *         set
	 */
	public BoaType getType() {
		return this.type;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "set of " + this.type;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "java.util.HashSet<" + this.type.toBoxedJavaType() + ">";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		final BoaSet other = (BoaSet) obj;
		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;
		return true;
	}
}
