package sizzle.types;

/**
 * A {@link CommentKindProtoMap}.
 * 
 * @author rdyer
 */
public class CommentKindProtoMap extends SizzleMap {
	/**
	 * Construct a {@link CommentKindProtoMap}.
	 */
	public CommentKindProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Ast.Comment.CommentKind";
	}
}
