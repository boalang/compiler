package boa.types;

/**
 * A type variable for use with functions that return types dependent on their argument types.
 * 
 * @author rdyer
 */
public class BoaTypeVar extends BoaScalar {
	private String name;

	public BoaTypeVar (String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(BoaType that) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "TypeVar " + name;
	}
}
