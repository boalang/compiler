package boa.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.statements.Statement;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class Program extends Node {
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

	public void addStatement(final Statement s) {
		s.setParent(this);
		statements.add(s);
	}

	/** {@inheritDoc} */
	@Override
	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
