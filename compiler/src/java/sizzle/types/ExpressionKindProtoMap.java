package sizzle.types;

/**
 * A {@link ExpressionKindProtoMap}.
 * 
 * @author rdyer
 */
public class ExpressionKindProtoMap extends SizzleProtoMap {
	/**
	 * Construct a {@link ExpressionKindProtoMap}.
	 */
	public ExpressionKindProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Expression.ExpressionKind";
	}
}
