package boa.compiler.ast;

import boa.compiler.SymbolTable;
import boa.compiler.ast.statements.Statement;
import boa.compiler.ast.statements.Block;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.types.BoaType;

/**
 * 
 * @author rdyer
 */
public abstract class Node {
	protected Node parent;

	public Node getParent() {
		return parent;
	}

	public void setParent(final Node parent) {
		this.parent = parent;
	}

	public int beginLine, beginColumn;
	public int endLine, endColumn;

	public Node setPositions(final int beginLine, final int beginColumn, final int endLine, final int endColumn) {
		this.beginLine = beginLine;
		this.beginColumn = beginColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
		return this;
	}

/*
	public Node setPositions(final Node first) {
		return setPositions(first.beginLine, first.beginColumn, first.endLine, first.endColumn);
	}

	public Node setPositions(final Token first) {
		return setPositions(first.beginLine, first.beginColumn, first.endLine, first.endColumn);
	}

	public Node setPositions(final Node first, final Node last) {
		return setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}

	public Node setPositions(final Node first, final Token last) {
		return setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}

	public Node setPositions(final Token first, final Token last) {
		return setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}

	public Node setPositions(final Token first, final Node last) {
		return setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
*/

	public BoaType type = null;
	public SymbolTable env = null;

	public abstract Node clone();

	public static Block ensureBlock(final Statement s) {
		if (s == null) return null;
		if (s instanceof Block) return (Block)s;
		return new Block().addStatement(s);
	}

	protected void copyFieldsTo(Node newNode) {
		newNode.type = type;
		newNode.env = env;
		newNode.beginLine = beginLine;
		newNode.beginColumn = beginColumn;
		newNode.endLine = endLine;
		newNode.endColumn = endColumn;
	}

	public abstract <A> void accept(final AbstractVisitor<A> v, final A arg);
	public abstract void accept(final AbstractVisitorNoArg v);

	public Node insertStatementBefore(final Statement s) {
		return insertStatementBefore(s, this);
	}

	public Node insertStatementBefore(final Statement s, final Node n) {
		return parent.insertStatementBefore(s, this);
	}

	public Node insertStatementAfter(final Statement s) {
		return insertStatementAfter(s, this);
	}

	public Node insertStatementAfter(final Statement s, final Node n) {
		return parent.insertStatementAfter(s, this);
	}

	public void replaceStatement(final Statement oldStmt, final Statement newStmt) {
		parent.replaceStatement(oldStmt, newStmt);
	}
}
