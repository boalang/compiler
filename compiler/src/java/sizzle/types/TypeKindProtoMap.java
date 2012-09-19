package sizzle.types;

/**
 * A {@link TypeKindProtoMap}.
 * 
 * @author rdyer
 */
public class TypeKindProtoMap extends SizzleProtoMap {
	/**
	 * Construct a {@link TypeKindProtoMap}.
	 */
	public TypeKindProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.TypeKind";
	}
}
