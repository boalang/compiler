package sizzle.types;

/**
 * A {@link SizzleType} representing a stack of values.
 * 
 * @author rdyer
 */
public class SizzleStack extends SizzleType {
	private final SizzleType type;

	/**
	 * Construct a {@link SizzleStack}.
	 */
	public SizzleStack() {
		this(null);
	}

	/**
	 * Construct a {@link SizzleStack}.
	 * 
	 * @param sizzleType
	 *            A {@link SizzleType} representing the type of the values in
	 *            this stack
	 */
	public SizzleStack(final SizzleType sizzleType) {
		this.type = sizzleType;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final SizzleType that) {
		// if that is a function, check the return value
		if (that instanceof SizzleFunction)
			return this.assigns(((SizzleFunction) that).getType());

		// otherwise, if that is not a stack, forget it
		if (!(that instanceof SizzleStack))
			return false;

		// same for the value type
		if (!((SizzleStack) that).type.assigns(this.type))
			return false;

		// ok
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final SizzleType that) {
		// if that is a function, check the return value
		if (that instanceof SizzleFunction)
			return this.assigns(((SizzleFunction) that).getType());

		// otherwise, if that is not a stack, forget it
		if (!(that instanceof SizzleStack))
			return false;

		// same for the value type
		if (!this.type.accepts(((SizzleStack) that).type))
			return false;

		// ok
		return true;
	}

	/**
	 * Get the type of the values of this stack.
	 * 
	 * @return A {@link SizzleType} representing the type of the values of this
	 *         stack
	 */
	public SizzleType getType() {
		return this.type;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "stack of " + this.type;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "java.util.Stack<" + this.type.toBoxedJavaType() + ">";
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
		final SizzleStack other = (SizzleStack) obj;
		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;
		return true;
	}
}
