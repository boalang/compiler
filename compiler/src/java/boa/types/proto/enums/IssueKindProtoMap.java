package boa.types.proto.enums;

import boa.types.BoaProtoMap;

/**
 * A {@link IssueKindProtoMap}.
 * 
 * @author rdyer
 */
public class IssueKindProtoMap extends BoaProtoMap {
	/**
	 * Construct a {@link IssueKindProtoMap}.
	 */
	public IssueKindProtoMap() {
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Issues.IssueRepository.IssueKind";
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAttribute(final String s) {
		try {
			return boa.types.Issues.IssueRepository.IssueKind.valueOf(s) != null;
		} catch (final Exception e) {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "IssueKind";
	}
}
