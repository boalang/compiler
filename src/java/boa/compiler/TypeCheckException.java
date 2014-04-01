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
	public Node n;
	public Node n2;

	/**
	 * Construct a TypeCheckException.
	 * 
	 * @param n
	 *            The {@link Node} where the error occurred
	 * @param text
	 *            A {@link String} containing the description of the error
	 */
	public TypeCheckException(final Node n, final String text) {
		super(text);
		this.n = n;
		this.n2 = n;
	}
	public TypeCheckException(final List<? extends Node> n, final String text) {
		super(text);
		this.n = n.get(0);
		this.n2 = n.get(n.size() - 1);
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
		super(text, e);
		this.n = n;
		this.n2 = n;
	}
	public TypeCheckException(final List<? extends Node> n, final String text, final Throwable e) {
		super(text, e);
		this.n = n.get(0);
		this.n2 = n.get(n.size() - 1);
	}
}
