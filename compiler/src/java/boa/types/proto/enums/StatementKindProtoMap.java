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

	@Override
	public String toJavaType() {
		return "boa.types.Ast.Statement.StatementKind";
	}
}
