package boa.types.proto.enums;

import boa.types.BoaProtoMap;

/**
 * A {@link ExpressionKindProtoMap}.
 * 
 * @author rdyer
 */
public class ExpressionKindProtoMap extends BoaProtoMap {
	/**
	 * Construct a {@link ExpressionKindProtoMap}.
	 */
	public ExpressionKindProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "boa.types.Ast.Expression.ExpressionKind";
	}
}
