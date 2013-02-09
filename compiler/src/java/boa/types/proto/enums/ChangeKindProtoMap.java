package boa.types.proto.enums;

import boa.types.BoaProtoMap;

/**
 * A {@link ChangeKindProtoMap}.
 * 
 * @author rdyer
 */
public class ChangeKindProtoMap extends BoaProtoMap {
	/**
	 * Construct a {@link ChangeKindProtoMap}.
	 */
	public ChangeKindProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "boa.types.Shared.ChangeKind";
	}
}
