package boa.types.proto.enums;

import boa.types.BoaProtoMap;

/**
 * A {@link ModifierKindProtoMap}.
 * 
 * @author rdyer
 */
public class ModifierKindProtoMap extends BoaProtoMap {
	/**
	 * Construct a {@link ModifierKindProtoMap}.
	 */
	public ModifierKindProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "boa.types.Ast.Modifier.ModifierKind";
	}
}
