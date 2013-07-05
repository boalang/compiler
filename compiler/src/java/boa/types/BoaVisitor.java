package boa.types;

/**
 * A {@link BoaType} that represents a visitor.
 * 
 * @author rdyer
 */
public class BoaVisitor extends BoaType {
	/**
	 * Construct a {@link BoaVisitor}.
	 */
	public BoaVisitor() {
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		if (!(that instanceof BoaVisitor))
			return false;

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "visitor";
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.runtime.BoaAbstractVisitor";
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
