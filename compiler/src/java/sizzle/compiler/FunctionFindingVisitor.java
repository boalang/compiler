package sizzle.compiler;

import java.util.ArrayList;
import java.util.List;

import sizzle.parser.syntaxtree.*;
import sizzle.parser.visitor.GJDepthFirst;
import sizzle.types.SizzleFunction;
import sizzle.types.SizzleType;

public class FunctionFindingVisitor extends GJDepthFirst<SizzleFunction, SymbolTable> {
	private final List<SizzleType> formalParameters;

	public FunctionFindingVisitor() {
		this(new ArrayList<SizzleType>());
	}

	public FunctionFindingVisitor(final List<SizzleType> formalParameters) {
		this.formalParameters = formalParameters;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Start n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Program n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Declaration n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final TypeDecl n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final StaticVarDecl n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final VarDecl n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Type n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Component n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final ArrayType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final TupleType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final SimpleTupleType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final SimpleMemberList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final SimpleMember n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final ProtoTupleType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final ProtoMemberList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final ProtoMember n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final ProtoFieldDecl n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final MapType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final OutputType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final ExprList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final FunctionType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Statement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Assignment n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Block n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final BreakStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final ContinueStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final DoStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final EmitStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final ExprStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final ForStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final IfStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final ResultStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final ReturnStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final SwitchStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final WhenStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final IdentifierList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final WhileStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Expression n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Conjunction n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Comparison n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final SimpleExpr n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Term n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Factor n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Selector n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Index n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Call n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final RegexpList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Regexp n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Operand n, final SymbolTable argu) {
		return n.f0.accept(this, argu);
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
	public SizzleFunction visit(final PairList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Pair n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Function n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final StatementExpr n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final Identifier n, final SymbolTable argu) {
		final String id = n.f0.tokenImage;

		return argu.getFunction(id, this.formalParameters);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final IntegerLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final FingerprintLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final FloatingPointLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final CharLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final StringLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final BytesLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final TimeLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final EmptyStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final StopStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final VisitorExpr n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final VisitorType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleFunction visit(final VisitStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}
}
