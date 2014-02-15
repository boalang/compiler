package boa.compiler.visitors;

import boa.compiler.ast.Call;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.ast.types.VisitorType;

/**
 * Analyze the code to see if it is simple (no visitors).
 * 
 * @author rdyer
 */
public class TaskClassifyingVisitor extends AbstractVisitorNoArg {
	private boolean hasVisitor = false;

	public boolean hasVisitor() {
		return hasVisitor;
	}

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		hasVisitor = false;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(Factor n) {
		if (n.getOperand() instanceof Identifier && ((Identifier)n.getOperand()).getToken().equals("getast"))
			if (n.getOpsSize() > 0 && n.getOp(0) instanceof Call) {
				hasVisitor = true;
				return;
			}

		super.visit(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorType n) {
		hasVisitor = true;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n) {
		hasVisitor = true;
	}
}
