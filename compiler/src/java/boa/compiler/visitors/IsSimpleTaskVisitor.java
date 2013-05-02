package boa.compiler.visitors;

import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.ast.types.VisitorType;

/**
 * Analyze the code to see if it is simple (no visitors).
 * 
 * @author rdyer
 */
public class IsSimpleTaskVisitor extends AbstractVisitorNoArg {
	private boolean isSimple = true;

	public boolean isSimple() {
		return isSimple;
	}

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		isSimple = true;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorType n) {
		isSimple = false;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n) {
		isSimple = false;
	}
}
