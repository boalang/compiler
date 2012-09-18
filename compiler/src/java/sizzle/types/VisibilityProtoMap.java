package sizzle.types;

/**
 * A {@link VisibilityProtoMap}.
 * 
 * @author rdyer
 */
public class VisibilityProtoMap extends SizzleMap {
	/**
	 * Construct a {@link VisibilityProtoMap}.
	 */
	public VisibilityProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Modifier.Visibility";
	}
}
