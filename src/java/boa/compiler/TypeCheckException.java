package boa.compiler;

import java.util.List;

import boa.compiler.ast.Node;

/**
 * An {@link Exception} thrown for type errors in Boa programs.
 * 
 * @author anthonyu
 * @author rdyer
 * 
 */
public class TypeCheckException extends RuntimeException {
	private static final long serialVersionUID = -5838752670934187621L;

	/**
	 * Construct a TypeCheckException.
	 * 
	 * @param n
	 *            The {@link Node} where the error occurred
	 * @param text
	 *            A {@link String} containing the description of the error
	 */
	public TypeCheckException(final Node n, final String text) {
		super(getMessage(n, text));
	}
	public TypeCheckException(final List<? extends Node> n, final String text) {
		super(getMessage(n, text));
	}

	/**
	 * Construct a TypeCheckException caused by another exception.
	 * 
	 * @param n
	 *            The {@link Node} where the error occurred
	 * @param text
	 *            A {@link String} containing the description of the error
	 * @param e
	 *            A {@link Throwable} representing the cause of this type
	 *            exception
	 */
	public TypeCheckException(final Node n, final String text, final Throwable e) {
		super(getMessage(n, text), e);
	}
	public TypeCheckException(final List<? extends Node> n, final String text, final Throwable e) {
		super(getMessage(n, text), e);
	}

	private static String getMessage(final Node n, final String text) {
		return "Error at lines " + n.beginLine + "-" + n.endLine +
				", columns " + n.beginColumn + "-" + n.endColumn + ": " + text;
	}
	private static String getMessage(final List<? extends Node> n, final String text) {
		return "Error at lines " + n.get(0).beginLine + "-" + n.get(n.size() - 1).endLine +
				", columns " + n.get(0).beginColumn + "-" + n.get(n.size() - 1).endColumn + ": " + text;
	}
}
