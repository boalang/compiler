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
package boa.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Node;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hridesh
 */
public class Block extends Statement {
	protected final List<Statement> statements = new ArrayList<Statement>();

	public List<Statement> getStatements() {
		return statements;
	}

	public int getStatementsSize() {
		return statements.size();
	}

	public Statement getStatement(final int index) {
		return statements.get(index);
	}

	public Block addStatement(final Statement s) {
		if (s != null) {
			s.setParent(this);
			statements.add(s);
		}
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public <T,A> T accept(final AbstractVisitor<T,A> v, A arg) {
		return v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public <A> void accept(final AbstractVisitorNoReturn<A> v, A arg) {
		v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public void accept(final AbstractVisitorNoArg v) {
		v.visit(this);
	}

	/** {@inheritDoc} */
	@Override
	public Node insertStatementBefore(final Statement s, final Node n) {
		int index = 0;
		for (; index < statements.size() && statements.get(index) != n; index++)
			;
		if (index == statements.size())
			return super.insertStatementBefore(s, n);
		s.setParent(this);
		statements.add(index, s);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Node insertStatementAfter(final Statement s, final Node n) {
		int index = 0;
		for (; index < statements.size() && statements.get(index) != n; index++)
			;
		if (index == statements.size())
			return super.insertStatementAfter(s, n);
		s.setParent(this);
		statements.add(index + 1, s);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public void replaceStatement(final Statement oldStmt, final Statement newStmt) {
		int index = 0;
		for (; index < statements.size() && statements.get(index) != oldStmt; index++)
			;
		if (index == statements.size())
			super.replaceStatement(oldStmt, newStmt);
		else {
			newStmt.setParent(this);
			statements.set(index, newStmt);
		}
	}

	public Block clone() {
		final Block b = new Block();
		for (final Statement s : statements)
			b.addStatement(s.clone());
		copyFieldsTo(b);
		return b;
	}
}
