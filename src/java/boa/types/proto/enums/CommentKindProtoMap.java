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

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Comment.CommentKind";
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAttribute(final String s) {
		try {
			return boa.types.Ast.Comment.CommentKind.valueOf(s) != null;
		} catch (final Exception e) {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "CommentKind";
	}
}
