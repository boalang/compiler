package boa.compiler.visitors;

import java.util.HashSet;
import java.util.Set;

import boa.compiler.ast.Call;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.expressions.VisitorExpression;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.ast.types.VisitorType;

import boa.types.BoaType;
import boa.types.proto.ASTRootProtoTuple;

/**
 * Analyze the code to see if it is simple (no visitors).
 * 
 * @author rdyer
 */
public class TaskClassifyingVisitor extends AbstractVisitorNoArg {
	protected final static Set<Class<? extends BoaType>> astTypes = new HashSet<Class<? extends BoaType>>();

	static {
		astTypes.addAll(new ASTRootProtoTuple().reachableTypes());
	}

	protected Set<Class<? extends BoaType>> types;

	private boolean complex = false;

	public boolean isComplex() {
		return complex;
	}

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		complex = false;
		types = new HashSet<Class<? extends BoaType>>();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(Factor n) {
		if (n.getOperand() instanceof Identifier && ((Identifier)n.getOperand()).getToken().equals("getast"))
			if (n.getOpsSize() > 0 && n.getOp(0) instanceof Call) {
				complex = true;
				return;
			}

		super.visit(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n) {
		if (n.hasWildcard())
			complex = true;
		else if (n.hasComponent())
			types.add(n.getComponent().getType().type.getClass());
		else
			for (final Identifier id : n.getIdList())
				types.add(id.type.getClass());

		super.visit(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorExpression n) {
		super.visit(n);

		types.retainAll(astTypes);
		if (!types.isEmpty())
			complex = true;
	}
}
