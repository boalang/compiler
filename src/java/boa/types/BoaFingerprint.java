package boa.types;

/**
 * A {@link BoaScalar} representing a 64 bit signature value.
 * 
 * @author anthonyu
 * 
 */
public class BoaFingerprint extends BoaScalar {
	/** {@inheritDoc} */
	@Override
	public BoaScalar arithmetics(final BoaType that) {
		// no math for fingerprints
		throw new RuntimeException("incorrect type " + this + " for arithmetic with " + that);
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		return this.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "long";
	}

	/** {@inheritDoc} */
	@Override
	public String toBoxedJavaType() {
		return "Long";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "fingerprint";
	}
}
