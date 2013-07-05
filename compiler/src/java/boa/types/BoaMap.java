package boa.types;

/**
 * A {@link BoaType} representing a mapping a set of keys to some value.
 * 
 * @author anthonyu
 * 
 */
public class BoaMap extends BoaType {
	private final BoaType type;
	private final BoaType indexType;

	/**
	 * Construct a BoaMap.
	 */
	public BoaMap() {
		this(null, null);
	}

	/**
	 * Construct a BoaMap.
	 * 
	 * @param boaType
	 *            A {@link BoaType} representing the type of the values in
	 *            this map
	 * 
	 * @param boaType2
	 *            A {@link BoaType} representing the type of the indices in
	 *            this map
	 */
	public BoaMap(final BoaType boaType, final BoaType boaType2) {
		this.type = boaType;
		this.indexType = boaType2;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		// if that is a function, check the return value
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		// otherwise, if that is not a map, forget it
		if (!(that instanceof BoaMap))
			return false;

		// if that index type is not equivalent this this's, forget it
		if (!((BoaMap) that).indexType.assigns(this.indexType))
			return false;

		// same for the value type
		if (!((BoaMap) that).type.assigns(this.type))
			return false;

		// ok
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		// if that is a function, check the return value
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		// otherwise, if that is not a map, forget it
		if (!(that instanceof BoaMap))
			return false;

		// if that index type is not equivalent this this's, forget it
		if (!this.indexType.accepts(((BoaMap) that).indexType))
			return false;

		// same for the value type
		if (!this.type.accepts(((BoaMap) that).type))
			return false;

		// ok
		return true;
	}

	/**
	 * Get the type of the values of this map.
	 * 
	 * @return A {@link BoaType} representing the type of the values of this
	 *         map
	 */
	public BoaType getType() {
		return this.type;
	}

	/**
	 * Get the type of the indices of this map.
	 * 
	 * @return A {@link BoaType} representing the type of the indices of this
	 *         map
	 */
	public BoaType getIndexType() {
		return this.indexType;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "map[" + this.indexType + "] of " + this.type;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "java.util.HashMap<" + this.indexType.toBoxedJavaType() + ", " + this.type.toBoxedJavaType() + ">";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.indexType == null ? 0 : this.indexType.hashCode());
		result = prime * result + (this.type == null ? 0 : this.type.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final BoaMap other = (BoaMap) obj;
		if (this.indexType == null) {
			if (other.indexType != null)
				return false;
		} else if (!this.indexType.equals(other.indexType))
			return false;
		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;
		return true;
	}
}
