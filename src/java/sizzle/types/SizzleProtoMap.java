package sizzle.types;

/**
 * A {@link SizzleProtoMap}.
 * 
 * @author rdyer
 * 
 */
public class SizzleProtoMap extends SizzleMap {
	/**
	 * Construct a SizzleProtoMap.
	 */
	public SizzleProtoMap() {
		super(new SizzleInt(), new SizzleString());
	}
}
