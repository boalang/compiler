package boa.compiler.ast.expressions;

import boa.compiler.ast.Node;
import boa.compiler.ast.Operand;
import boa.compiler.ast.statements.Block;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.parser.Token;

/**
 * 
 * @author rdyer
 */
public class StatementExpr extends Operand {
	protected Block b;

	public Block getBlock() {
		return b;
	}

	public StatementExpr (final Block b) {
		if (b != null)
			b.setParent(this);
		this.b = b;
	}

	/** {@inheritDoc} */
	@Override
	public <A> void accept(final AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public void accept(final AbstractVisitorNoArg v) {
		v.visit(this);
	}

	public StatementExpr clone() {
		final StatementExpr e = new StatementExpr(b.clone());
		copyFieldsTo(e);
		return e;
	}

	public StatementExpr setPositions(final Token first, final Node last) {
		return (StatementExpr)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}
