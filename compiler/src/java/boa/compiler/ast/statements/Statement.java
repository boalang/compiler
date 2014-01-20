package boa.compiler.ast.statements;

import boa.compiler.ast.Node;
import boa.parser.Token;

/**
 * 
 * @author rdyer
 */
public abstract class Statement extends Node {
	public abstract Statement clone();

	public Statement setPositions(final Node first, final Token last) {
		return (Statement)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
