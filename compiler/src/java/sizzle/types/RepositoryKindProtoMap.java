package sizzle.types;

/**
 * A {@link RepositoryKindProtoMap}.
 * 
 * @author rdyer
 * 
 */
public class RepositoryKindProtoMap extends SizzleProtoMap {
	/**
	 * Construct a {@link RepositoryKindProtoMap}.
	 */
	public RepositoryKindProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Code.CodeRepository.RepositoryKind";
	}
}
