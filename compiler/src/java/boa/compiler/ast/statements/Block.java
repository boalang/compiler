package boa.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Node;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class Block extends Statement {
	protected final List<Node> statements = new ArrayList<Node>();

	public List<Node> getStatements() {
		return statements;
	}

	public int getStatementsSize() {
		return statements.size();
	}

	public Node getStatement(final int index) {
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
