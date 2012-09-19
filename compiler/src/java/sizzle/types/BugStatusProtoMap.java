package sizzle.types;

/**
 * A {@link BugStatusProtoMap}.
 * 
 * @author rdyer
 */
public class BugStatusProtoMap extends SizzleProtoMap {
	/**
	 * Construct a BugStatusProtoMap.
	 */
	public BugStatusProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Bugs.Bug.BugStatus";
	}
}
