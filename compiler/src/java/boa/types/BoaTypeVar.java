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

	@Override
	public String toString() {
		return "TypeVar " + name;
	}
}
