package boa.types.proto.enums;

import boa.types.BoaProtoMap;

/**
 * A {@link RepositoryKindProtoMap}.
 * 
 * @author rdyer
 * 
 */
public class RepositoryKindProtoMap extends BoaProtoMap {
	/**
	 * Construct a {@link RepositoryKindProtoMap}.
	 */
	public RepositoryKindProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "boa.types.Code.CodeRepository.RepositoryKind";
	}
}
