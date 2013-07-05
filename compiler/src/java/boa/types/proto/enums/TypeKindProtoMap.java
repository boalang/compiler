package boa.types.proto.enums;

import boa.types.BoaProtoMap;

/**
 * A {@link TypeKindProtoMap}.
 * 
 * @author rdyer
 */
public class TypeKindProtoMap extends BoaProtoMap {
	/**
	 * Construct a {@link TypeKindProtoMap}.
	 */
	public TypeKindProtoMap() {
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.TypeKind";
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAttribute(final String s) {
		try {
			return boa.types.Ast.TypeKind.valueOf(s) != null;
		} catch (final Exception e) {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "TypeKind";
	}
}
