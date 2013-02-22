package boa.types;

/**
 * A {@link BoaMap}, which is actually a Protocol Buffer enum.
 * 
 * @author rdyer
 * 
 */
public class BoaProtoMap extends BoaMap {
	/**
	 * Construct a {@link BoaProtoMap}.
	 */
	public BoaProtoMap() {
		super(new BoaInt(), new BoaString());
	}

	/**
	 * Returns if this protobuf enum has the specified attribute.
	 * 
	 * @param s the attribute to check for
	 * @return true if it has the attribute s
	 */
	public boolean hasAttribute(final String s) {
		throw new RuntimeException("proto map must override hasAttribute()");
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(BoaType obj) {
		return this.getClass() == obj.getClass();
	}

	/** {@inheritDoc} */
	@Override
	public boolean compares(BoaType obj) {
		return this.getClass() == obj.getClass();
	}
}
