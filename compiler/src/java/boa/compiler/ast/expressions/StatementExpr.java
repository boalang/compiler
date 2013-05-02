package boa.compiler.ast.expressions;

import boa.compiler.ast.Operand;
import boa.compiler.ast.statements.Block;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

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
		b.setParent(this);
		this.b = b;
	}

	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
