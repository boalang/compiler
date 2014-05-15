package boa.types;

/**
 * A {@link BoaScalar} representing a string of bytes.
 * 
 * @author anthonyu
 * 
 */
public class BoaBytes extends BoaScalar {
	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		return this.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "byte[]";
	}

	/** {@inheritDoc} */
	@Override
	public String toBoxedJavaType() {
		return "Byte[]";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "bytes";
	}
}