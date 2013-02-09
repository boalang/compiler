package boa.types.proto.enums;

import boa.types.BoaProtoMap;

/**
 * A {@link CommentKindProtoMap}.
 * 
 * @author rdyer
 */
public class CommentKindProtoMap extends BoaProtoMap {
	/**
	 * Construct a {@link CommentKindProtoMap}.
	 */
	public CommentKindProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "boa.types.Ast.Comment.CommentKind";
	}
}
