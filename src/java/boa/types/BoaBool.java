package boa.types;

/**
 * A {@link BoaScalar} representing a true/false value.
 * 
 * @author anthonyu
 * 
 */
public class BoaBool extends BoaScalar {
	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		return this.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boolean";
	}

	/** {@inheritDoc} */
	@Override
	public String toBoxedJavaType() {
		return "Boolean";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "bool";
	}
}
