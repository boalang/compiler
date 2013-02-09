package boa.types.proto.enums;

import boa.types.BoaProtoMap;

/**
 * A {@link BugStatusProtoMap}.
 * 
 * @author rdyer
 */
public class BugStatusProtoMap extends BoaProtoMap {
	/**
	 * Construct a BugStatusProtoMap.
	 */
	public BugStatusProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "boa.types.Bugs.Bug.BugStatus";
	}
}
