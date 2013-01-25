package sizzle.runtime;

/**
 * The Java runtime type of a void Boa function.
 * 
 * @author rdyer
 */
public abstract class SizzleVoidFunc {
	public abstract void invoke(Object[] args) throws Exception;
}
