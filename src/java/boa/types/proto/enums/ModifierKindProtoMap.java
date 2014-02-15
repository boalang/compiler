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

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Modifier.ModifierKind";
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAttribute(final String s) {
		try {
			return boa.types.Ast.Modifier.ModifierKind.valueOf(s) != null;
		} catch (final Exception e) {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ModifierKind";
	}
}
