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

	@Override
	public String toJavaType() {
		return "boa.types.Diff.ChangedFile.FileKind";
	}
}
