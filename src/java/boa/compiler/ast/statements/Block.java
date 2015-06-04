package boa.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Node;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;
import boa.parser.Token;

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

	public Block setPositions(final Token first, final Token last) {
		return (Block)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
