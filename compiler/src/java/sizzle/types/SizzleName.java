package sizzle.types;

public class SizzleName extends SizzleScalar {
	private final SizzleType type;
	private final String id;

	public SizzleName(final SizzleType type, final String id) {
		this.type = type;
		this.id = id;
	}

	public SizzleName(final SizzleType type) {
		this(type, type.toString());
	}

	public SizzleType getType() {
		return this.type;
	}

	public String getId() {
		return this.id;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final SizzleType that) {
		if (that instanceof SizzleName)
			return this.type.assigns(((SizzleName) that).getType());
		return this.type.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final SizzleType that) {
		return this.type.accepts(that);
	}

	@Override
	public String toString() {
		return this.id + ":" + this.type;
	}

	@Override
	public String toJavaType() {
		return this.type.toJavaType();
	}

	@Override
	public String toBoxedJavaType() {
		return this.type.toBoxedJavaType();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (this.id == null ? 0 : this.id.hashCode());
		result = prime * result + (this.type == null ? 0 : this.type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final SizzleName other = (SizzleName) obj;
		if (this.id == null) {
			if (other.id != null)
				return false;
		} else if (!this.id.equals(other.id))
			return false;
		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;
		return true;
	}
}
