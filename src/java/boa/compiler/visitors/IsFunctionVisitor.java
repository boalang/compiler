package boa.compiler.visitors;

import boa.compiler.ast.expressions.FunctionExpression;

/**
 * 
 * @author rdyer
 */
public class IsFunctionVisitor extends AbstractVisitorNoArg {
	private boolean isFunction;

	public boolean isFunction() {
		return isFunction;
	}

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		isFunction = false;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionExpression n) {
		isFunction = true;
	}
}
