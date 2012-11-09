package sizzle.types;

/**
 * A {@link ChangeKindProtoMap}.
 * 
 * @author rdyer
 */
public class ChangeKindProtoMap extends SizzleProtoMap {
	/**
	 * Construct a {@link ChangeKindProtoMap}.
	 */
	public ChangeKindProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Shared.ChangeKind";
	}
}
