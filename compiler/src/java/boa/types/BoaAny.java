package boa.types;

/**
 * A {@link BoaType} representing the wildcard or any type.
 * 
 * @author anthonyu
 * 
 */
public class BoaAny extends BoaType {
	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		// anything can be assigned to a variable of type 'any'
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		// anything can be be used as an 'any' param
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "any";
	}
}