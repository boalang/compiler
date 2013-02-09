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

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Modifier.Visibility";
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAttribute(final String s) {
		try {
			return boa.types.Ast.Modifier.Visibility.valueOf(s) != null;
		} catch (final Exception e) {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Visibility";
	}
}
