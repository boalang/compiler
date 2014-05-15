package boa.types.proto.enums;

import boa.types.BoaProtoMap;

/**
 * A {@link StatementKindProtoMap}.
 * 
 * @author rdyer
 */
public class StatementKindProtoMap extends BoaProtoMap {
	/**
	 * Construct a {@link StatementKindProtoMap}.
	 */
	public StatementKindProtoMap() {
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Statement.StatementKind";
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAttribute(final String s) {
		try {
			return boa.types.Ast.Statement.StatementKind.valueOf(s) != null;
		} catch (final Exception e) {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "StatementKind";
	}
}
