/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.compiler.ast;

import boa.compiler.SymbolTable;
import boa.compiler.ast.statements.Statement;
import boa.compiler.ast.statements.Block;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;
import boa.types.BoaType;

/**
 * 
 * @author rdyer
 * @author hridesh
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

	public abstract <T,A> T accept(final AbstractVisitor<T,A> v, final A arg);
	public abstract <A> void accept(final AbstractVisitorNoReturn<A> v, final A arg);
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
