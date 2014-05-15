package boa.compiler;

import java.util.List;

import boa.types.BoaFunction;
import boa.types.BoaType;

import boa.parser.syntaxtree.*;

public class FunctionFindingVisitor extends DefaultVisitor<BoaFunction, SymbolTable> {
	private final List<BoaType> formalParameters;

	public FunctionFindingVisitor(final List<BoaType> formalParameters) {
		this.formalParameters = formalParameters;
	}

	/** {@inheritDoc} */
	@Override
	public BoaFunction visitOperandFactor(final NodeToken op, final Factor n, final SymbolTable argu) {
		return n.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public BoaFunction visitOperandParen(final Expression n, final SymbolTable argu) {
		return n.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public BoaFunction visit(final Composite n, final SymbolTable argu) {
		if (n.f1.present())
			return n.f1.accept(this, argu);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public BoaFunction visit(final Identifier n, final SymbolTable argu) {
		return argu.getFunction(n.f0.tokenImage, this.formalParameters);
	}
}
