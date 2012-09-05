package sizzle.types;

/**
 * A {@link FileTypeProtoMap}.
 * 
 * @author rdyer
 * 
 */
public class FileTypeProtoMap extends SizzleMap {
	/**
	 * Construct a FileTypeProtoMap.
	 */
	public FileTypeProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.File.FileType";
	}
}
