package boa.types;

/**
 * A {@link BoaType} representing an array of scalar values.
 * 
 * @author anthonyu
 * 
 */
public class BoaVarargs extends BoaType {
	private final BoaType type;

	/**
	 * Construct a BoaVarargs.
	 * 
	 * @param type
	 *            A {@link BoaScalar} representing the type of the elements
	 *            in this array
	 */
	public BoaVarargs(final BoaType type) {
		this.type = type;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		// varargs can only accept, not be assigned to
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		// if that is a function, check its return type
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		if (this.type.accepts(that))
			return true;

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean compares(final BoaType that) {
		// varargs don't need to compare each other
		return false;
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

		final BoaVarargs other = (BoaVarargs) obj;

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
		return "varargs of " + this.type.toString();
	}
}
