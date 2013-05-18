package boa.compiler.ast.statements;

import boa.compiler.ast.Node;

/**
 * 
 * @author rdyer
 */
public abstract class Statement extends Node {
	public abstract Statement clone();
}
