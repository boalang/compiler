package sizzle.types;

/**
 * A type variable for use with functions that return types dependent on their argument types.
 * 
 * @author rdyer
 */
public class SizzleTypeVar extends SizzleScalar {
	private String name;

	public SizzleTypeVar (String name) {
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
