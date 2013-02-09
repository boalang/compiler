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

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Shared.ChangeKind";
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAttribute(final String s) {
		try {
			return boa.types.Shared.ChangeKind.valueOf(s) != null;
		} catch (final Exception e) {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ChangeKind";
	}
}
