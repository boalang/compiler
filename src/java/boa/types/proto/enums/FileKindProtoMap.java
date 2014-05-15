package boa.types.proto.enums;

import boa.types.BoaProtoMap;

/**
 * A {@link FileKindProtoMap}.
 * 
 * @author rdyer
 */
public class FileKindProtoMap extends BoaProtoMap {
	/**
	 * Construct a {@link FileKindProtoMap}.
	 */
	public FileKindProtoMap() {
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Diff.ChangedFile.FileKind";
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAttribute(final String s) {
		try {
			return boa.types.Diff.ChangedFile.FileKind.valueOf(s) != null;
		} catch (final Exception e) {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "FileKind";
	}
}
