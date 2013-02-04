package sizzle.compiler;

import sizzle.parser.syntaxtree.*;
import sizzle.parser.visitor.GJDepthFirst;

public class IsFunctionVisitor extends GJDepthFirst<Boolean, SymbolTable> {
	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Start n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Program n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Declaration n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final TypeDecl n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final StaticVarDecl n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final VarDecl n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Type n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Component n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final ArrayType n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final TupleType n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final SimpleTupleType n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final SimpleMemberList n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final SimpleMember n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final ProtoTupleType n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final ProtoMemberList n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final ProtoMember n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final ProtoFieldDecl n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final MapType n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final OutputType n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final ExprList n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final FunctionType n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Statement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Assignment n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Block n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final BreakStatement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final ContinueStatement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final DoStatement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final EmitStatement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final ExprStatement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final ForStatement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final IfStatement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final ResultStatement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final ReturnStatement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final SwitchStatement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final WhenStatement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final WhenKind n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final IdentifierList n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final WhileStatement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Expression n, final SymbolTable argu) {
		return n.f0.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Conjunction n, final SymbolTable argu) {
		return n.f0.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Comparison n, final SymbolTable argu) {
		return n.f0.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final SimpleExpr n, final SymbolTable argu) {
		return n.f0.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Term n, final SymbolTable argu) {
		return n.f0.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Factor n, final SymbolTable argu) {
		return n.f0.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Selector n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Index n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Call n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final RegexpList n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Regexp n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Operand n, final SymbolTable argu) {
		return n.f0.choice.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Composite n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final PairList n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Pair n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Function n, final SymbolTable argu) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final StatementExpr n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Identifier n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final IntegerLiteral n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final FingerprintLiteral n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final FloatingPointLiteral n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final CharLiteral n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final StringLiteral n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final BytesLiteral n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final TimeLiteral n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final EmptyStatement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final StopStatement n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final VisitorExpr n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final VisitorType n, final SymbolTable argu) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final VisitStatement n, final SymbolTable argu) {
		return false;
	}
}
