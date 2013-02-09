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

	@Override
	public String toJavaType() {
		return "boa.types.Ast.TypeKind";
	}
}
