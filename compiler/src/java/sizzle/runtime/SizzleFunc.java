package sizzle.runtime;

/**
 * The Java runtime type of a Boa function that returns a value of type R.
 * 
 * @author rdyer
 */
public abstract class SizzleFunc<R> {
	public abstract R invoke(Object[] args) throws Exception;
}
