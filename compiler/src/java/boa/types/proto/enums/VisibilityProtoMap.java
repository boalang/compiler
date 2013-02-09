package boa.types.proto.enums;

import boa.types.BoaProtoMap;

/**
 * A {@link VisibilityProtoMap}.
 * 
 * @author rdyer
 */
public class VisibilityProtoMap extends BoaProtoMap {
	/**
	 * Construct a {@link VisibilityProtoMap}.
	 */
	public VisibilityProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "boa.types.Ast.Modifier.Visibility";
	}
}
