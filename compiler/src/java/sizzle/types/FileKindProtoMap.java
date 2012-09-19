package sizzle.types;

/**
 * A {@link FileKindProtoMap}.
 * 
 * @author rdyer
 */
public class FileKindProtoMap extends SizzleProtoMap {
	/**
	 * Construct a {@link FileKindProtoMap}.
	 */
	public FileKindProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.File.FileKind";
	}
}
