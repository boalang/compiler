package sizzle.compiler;

import java.util.List;

import sizzle.parser.syntaxtree.*;
import sizzle.types.SizzleFunction;
import sizzle.types.SizzleType;

public class FunctionFindingVisitor extends DefaultVisitor<SizzleFunction, SymbolTable> {
	private final List<SizzleType> formalParameters;

	public FunctionFindingVisitor(final List<SizzleType> formalParameters) {
		this.formalParameters = formalParameters;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visitOperandFactor(final NodeToken op, final Factor n, final SymbolTable argu) {
		return n.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visitOperandParen(final Expression n, final SymbolTable argu) {
		return n.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Composite n, final SymbolTable argu) {
		if (n.f1.present())
			return n.f1.accept(this, argu);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Identifier n, final SymbolTable argu) {
		return argu.getFunction(n.f0.tokenImage, this.formalParameters);
	}
}
