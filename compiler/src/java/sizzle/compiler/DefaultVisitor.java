package sizzle.compiler;

import java.util.Vector;

import sizzle.parser.syntaxtree.*;
import sizzle.parser.visitor.GJDepthFirst;

public class DefaultVisitor<RET, ARG> extends GJDepthFirst<RET, ARG> {
	/** {@inheritDoc} */
	@Override
	public RET visit(final Start n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Program n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Declaration n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final TypeDecl n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final StaticVarDecl n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final VarDecl n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Type n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Component n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final ArrayType n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final TupleType n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final SimpleTupleType n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final SimpleMemberList n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final SimpleMember n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final ProtoTupleType n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final ProtoMemberList n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final ProtoMember n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final ProtoFieldDecl n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final MapType n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final StackType n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final OutputType n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final ExprList n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final FunctionType n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Statement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Assignment n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Block n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final BreakStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final ContinueStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final DoStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final EmitStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final ExprStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final ForStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final IfStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final ResultStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final ReturnStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final SwitchStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final WhenStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final WhenKind n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final IdentifierList n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final WhileStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Expression n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Conjunction n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Comparison n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final SimpleExpr n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Term n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Factor n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Selector n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Index n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Call n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final RegexpList n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Regexp n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Operand n, final ARG argu) {
		switch (n.f0.which) {
		case 0: // identifier
		case 1: // string literal
		case 2: // integer literal
		case 3: // floating point literal
		case 4: // composite
		case 5: // visitor
		case 6: // function
		case 9: // statement expression
			return n.f0.choice.accept(this, argu);
		case 8: // $
			return visitOperandDollar(n, argu);
		case 7: // unary operator
			final Vector<Node> nodes = ((NodeSequence) n.f0.choice).nodes;
			return visitOperandFactor((NodeToken)((NodeChoice)nodes.elementAt(0)).choice, (Factor)nodes.elementAt(1), argu);
		case 10: // parenthetical
			return visitOperandParen((Expression)((NodeSequence) n.f0.choice).nodes.elementAt(1), argu);
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	public RET visitOperandDollar(final Operand n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	public RET visitOperandFactor(final NodeToken op, final Factor n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	public RET visitOperandParen(final Expression n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Composite n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final PairList n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Pair n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Function n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final StatementExpr n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final Identifier n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final IntegerLiteral n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final FingerprintLiteral n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final FloatingPointLiteral n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final CharLiteral n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final StringLiteral n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final BytesLiteral n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final TimeLiteral n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final EmptyStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final StopStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final VisitorExpr n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final VisitorType n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public RET visit(final VisitStatement n, final ARG argu) {
		throw new RuntimeException("unimplemented");
	}
}
