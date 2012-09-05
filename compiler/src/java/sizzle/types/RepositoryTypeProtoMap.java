package sizzle.types;

/**
 * A {@link RepositoryTypeProtoMap}.
 * 
 * @author rdyer
 * 
 */
public class RepositoryTypeProtoMap extends SizzleProtoMap {
	/**
	 * Construct a RepositoryTypeProtoMap.
	 */
	public RepositoryTypeProtoMap() {
	}

	@Override
	public String toJavaType() {
		return "sizzle.types.Code.CodeRepository.RepositoryType";
	}
}
