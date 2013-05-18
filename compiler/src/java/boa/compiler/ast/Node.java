package boa.compiler.ast;

import boa.compiler.SymbolTable;
import boa.compiler.ast.statements.Statement;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.parser.syntaxtree.NodeToken;
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

	protected int beginLine, beginColumn;
	protected int endLine, endColumn;

	public Node setPositions(final int beginLine, final int beginColumn, final int endLine, final int endColumn) {
		this.beginLine = beginLine;
		this.beginColumn = beginColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
		return this;
	}

	public Node setPositions(final NodeToken first, final NodeToken last) {
		return setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}

	public int getBeginLine() {
		return beginLine;
	}

	public int getBeginColumn() {
		return beginColumn;
	}

	public int getEndLine() {
		return endLine;
	}

	public int getEndColumn() {
		return endColumn;
	}

	public BoaType type = null;
	public SymbolTable env = null;

	public abstract Node clone();

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
