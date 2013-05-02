package boa.compiler.ast;

import boa.compiler.SymbolTable;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.types.BoaType;

/**
 * 
 * @author rdyer
 */
public abstract class Node {
	protected Node parent;
	protected int beginLine, beginColumn;
	protected int endLine, endColumn;

	public Node getParent() {
		return parent;
	}

	public void setParent(final Node parent) {
		this.parent = parent;
	}

	public Node setPositions(final int beginLine, final int beginColumn, final int endLine, final int endColumn) {
		this.beginLine = beginLine;
		this.beginColumn = beginColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
		return this;
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

	public abstract <A> void accept(AbstractVisitor<A> v, A arg);
	public abstract void accept(AbstractVisitorNoArg v);

	public BoaType type = null;
	public SymbolTable env = null;
}
