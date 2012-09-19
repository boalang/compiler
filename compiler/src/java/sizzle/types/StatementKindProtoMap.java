package sizzle.types;

/**
 * A {@link StatementKindProtoMap}.
 * 
 * @author rdyer
 */
public class StatementKindProtoMap extends SizzleProtoMap {
	/**
	 * Construct a {@link StatementKindProtoMap}.
	 */
	public StatementKindProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Statement.StatementKind";
	}
}
