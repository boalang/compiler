package boa.compiler.visitors;

import java.util.List;

import boa.compiler.SymbolTable;
import boa.compiler.ast.Identifier;

import boa.types.BoaFunction;
import boa.types.BoaType;

/**
 * 
 * @author rdyer
 */
public class FunctionFindingVisitor extends AbstractVisitor<SymbolTable> {
	private BoaFunction func;
	private final List<BoaType> formalParameters;

	public boolean hasFunction() {
		return func != null;
	}

	public BoaFunction getFunction() {
		return func;
	}

	public FunctionFindingVisitor(final List<BoaType> formalParameters) {
		this.formalParameters = formalParameters;
	}

	/** {@inheritDoc} */
	@Override
	protected void initialize(SymbolTable arg) {
		func = null;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Identifier n, final SymbolTable arg) {
		func = arg.getFunction(n.getToken(), this.formalParameters);
	}
}
