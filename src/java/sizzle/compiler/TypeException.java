package sizzle.compiler;

import sizzle.parser.syntaxtree.Node;

/**
 * An {@link Exception} thrown for type errors in Sizzle programs.
 * 
 * @author anthonyu
 * @author rdyer
 * 
 */
public class TypeException extends RuntimeException {
	private static final long serialVersionUID = -5838752670934187621L;

	/**
	 * Construct a TypeException.
	 * 
	 * @param n
	 *            The {@link Node} where the error occurred
	 * @param text
	 *            A {@link String} containing the description of the error
	 */
	public TypeException(final Node n, final String text) {
		super(getMessage(n) + text);
	}

	/**
	 * Construct a TypeException caused by another exception.
	 * 
	 * @param n
	 *            The {@link Node} where the error occurred
	 * @param text
	 *            A {@link String} containing the description of the error
	 * @param e
	 *            A {@link Throwable} representing the cause of this type
	 *            exception
	 */
	public TypeException(final Node n, final String text, final Throwable e) {
		super(getMessage(n) + text, e);
	}

	private static String getMessage(final Node n) {
		// FIXME at some point we want to list locations of errors
		return "";
	}
}
