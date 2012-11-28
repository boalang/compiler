package sizzle.runtime;

public abstract class SizzleFunc<R> {
	public abstract R invoke(Object[] args) throws Exception;
}
