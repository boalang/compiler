package boa.types;

/**
 * A {@link BoaScalar} representing a 64 bit integer value.
 * 
 * @author anthonyu
 * 
 */
public class BoaInt extends BoaScalar {
	/** {@inheritDoc} */
	@Override
	public BoaScalar arithmetics(final BoaType that) {
		// if that is a function, check its return value
		if (that instanceof BoaFunction)
			return this.arithmetics(((BoaFunction) that).getType());
		// otherwise, if it is an int, the type is int
		else if (that instanceof BoaInt)
			return new BoaInt();
		// otherwise, if it's a time, the type is time
		else if (that instanceof BoaTime)
			return new BoaTime();
		// otherwise if it's a float, the type is float
		else if (that instanceof BoaFloat)
			return new BoaFloat();

		// otherwise, check the default
		return super.arithmetics(that);
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		return this.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "int";
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "long";
	}

	/** {@inheritDoc} */
	@Override
	public String toBoxedJavaType() {
		return "Long";
	}
}
