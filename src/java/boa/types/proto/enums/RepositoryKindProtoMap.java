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

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Code.CodeRepository.RepositoryKind";
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAttribute(final String s) {
		try {
			return boa.types.Code.CodeRepository.RepositoryKind.valueOf(s) != null;
		} catch (final Exception e) {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "RepositoryKind";
	}
}
