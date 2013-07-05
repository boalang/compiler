package boa.types;

/**
 * A {@link BoaScalar} representing a string of characters.
 * 
 * @author anthonyu
 * 
 */
public class BoaString extends BoaScalar {
	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		return this.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "String";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "string";
	}
}
