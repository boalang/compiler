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

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Expression.ExpressionKind";
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAttribute(final String s) {
		try {
			return boa.types.Ast.Expression.ExpressionKind.valueOf(s) != null;
		} catch (final Exception e) {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ExpressionKind";
	}
}
