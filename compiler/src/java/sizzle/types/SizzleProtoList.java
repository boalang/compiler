package sizzle.types;

/**
 * A {@link SizzleType} representing an array of scalar values that is a SizzleProtoTuple member.
 * 
 * @author rdyer
 * 
 */
public class SizzleProtoList extends SizzleArray {
	/**
	 * Construct a SizzleProtoList.
	 */
	public SizzleProtoList() {
	}

	/**
	 * Construct a SizzleProtoList.
	 * 
	 * @param sizzleType
	 *            A {@link SizzleType} representing the type of the elements in
	 *            this array
	 */
	public SizzleProtoList(final SizzleType sizzleType) {
		super(sizzleType);
	}
}
