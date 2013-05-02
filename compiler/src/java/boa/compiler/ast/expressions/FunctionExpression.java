package boa.compiler.ast.expressions;

import boa.compiler.ast.Operand;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.types.FunctionType;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class FunctionExpression extends Operand {
	protected FunctionType t;
	protected Block body;

	public FunctionType getType() {
		return t;
	}

	public Block getBody() {
		return body;
	}

	public FunctionExpression (final FunctionType t, final Block body) {
		t.setParent(this);
		body.setParent(this);
		this.t = t;
		this.body = body;
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
