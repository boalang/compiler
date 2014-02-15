package boa.types;

/**
 * A {@link BoaScalar} representing an double precision floating point value.
 * 
 * @author anthonyu
 * 
 */
public class BoaFloat extends BoaScalar {
	/** {@inheritDoc} */
	@Override
	public BoaScalar arithmetics(final BoaType that) {
		// if that is a function, check its return type
		if (that instanceof BoaFunction)
			return this.arithmetics(((BoaFunction) that).getType());

		// if it's a float, the type is float
		if (that instanceof BoaFloat)
			return new BoaFloat();

		// same with ints
		if (that instanceof BoaInt)
			return new BoaFloat();

		return super.arithmetics(that);
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		// ints can be assigned to floats
		if (that instanceof BoaInt)
			return true;

		// otherwise, just check the defaults
		return super.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		return this.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "double";
	}

	/** {@inheritDoc} */
	@Override
	public String toBoxedJavaType() {
		return "Double";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "float";
	}
}
