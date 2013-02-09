package boa.types;

/**
 * A {@link BoaProtoMap}.
 * 
 * @author rdyer
 * 
 */
public class BoaProtoMap extends BoaMap {
	/**
	 * Construct a BoaProtoMap.
	 */
	public BoaProtoMap() {
		super(new BoaInt(), new BoaString());
	}
}
