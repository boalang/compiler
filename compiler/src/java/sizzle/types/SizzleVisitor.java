package sizzle.types;

/**
 * A {@link SizzleType} that represents a visitor.
 * 
 * @author rdyer
 */
public class SizzleVisitor extends SizzleType {
	/**
	 * Construct a {@link SizzleVisitor}.
	 */
	public SizzleVisitor() {
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final SizzleType that) {
		if (!(that instanceof SizzleVisitor))
			return false;

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		// TODO ??
		return "visitor";
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "sizzle.runtime.BoaAbstractVisitor";
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
		return true;
	}
}
