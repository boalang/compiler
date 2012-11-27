package sizzle.compiler;

import sizzle.parser.syntaxtree.ArrayType;
import sizzle.parser.syntaxtree.Assignment;
import sizzle.parser.syntaxtree.Block;
import sizzle.parser.syntaxtree.BreakStatement;
import sizzle.parser.syntaxtree.BytesLiteral;
import sizzle.parser.syntaxtree.Call;
import sizzle.parser.syntaxtree.CharLiteral;
import sizzle.parser.syntaxtree.Comparison;
import sizzle.parser.syntaxtree.Component;
import sizzle.parser.syntaxtree.Composite;
import sizzle.parser.syntaxtree.Conjunction;
import sizzle.parser.syntaxtree.ContinueStatement;
import sizzle.parser.syntaxtree.Declaration;
import sizzle.parser.syntaxtree.DoStatement;
import sizzle.parser.syntaxtree.EmitStatement;
import sizzle.parser.syntaxtree.ExprList;
import sizzle.parser.syntaxtree.ExprStatement;
import sizzle.parser.syntaxtree.Expression;
import sizzle.parser.syntaxtree.Factor;
import sizzle.parser.syntaxtree.FingerprintLiteral;
import sizzle.parser.syntaxtree.FloatingPointLiteral;
import sizzle.parser.syntaxtree.ForStatement;
import sizzle.parser.syntaxtree.Function;
import sizzle.parser.syntaxtree.FunctionType;
import sizzle.parser.syntaxtree.Identifier;
import sizzle.parser.syntaxtree.IdentifierList;
import sizzle.parser.syntaxtree.IfStatement;
import sizzle.parser.syntaxtree.Index;
import sizzle.parser.syntaxtree.IntegerLiteral;
import sizzle.parser.syntaxtree.MapType;
import sizzle.parser.syntaxtree.Operand;
import sizzle.parser.syntaxtree.OutputType;
import sizzle.parser.syntaxtree.Pair;
import sizzle.parser.syntaxtree.PairList;
import sizzle.parser.syntaxtree.Program;
import sizzle.parser.syntaxtree.ProtoFieldDecl;
import sizzle.parser.syntaxtree.ProtoMember;
import sizzle.parser.syntaxtree.ProtoMemberList;
import sizzle.parser.syntaxtree.ProtoTupleType;
import sizzle.parser.syntaxtree.Regexp;
import sizzle.parser.syntaxtree.RegexpList;
import sizzle.parser.syntaxtree.ResultStatement;
import sizzle.parser.syntaxtree.ReturnStatement;
import sizzle.parser.syntaxtree.Selector;
import sizzle.parser.syntaxtree.SimpleExpr;
import sizzle.parser.syntaxtree.SimpleMember;
import sizzle.parser.syntaxtree.SimpleMemberList;
import sizzle.parser.syntaxtree.SimpleTupleType;
import sizzle.parser.syntaxtree.Start;
import sizzle.parser.syntaxtree.Statement;
import sizzle.parser.syntaxtree.StatementExpr;
import sizzle.parser.syntaxtree.StaticVarDecl;
import sizzle.parser.syntaxtree.StringLiteral;
import sizzle.parser.syntaxtree.SwitchStatement;
import sizzle.parser.syntaxtree.Term;
import sizzle.parser.syntaxtree.TimeLiteral;
import sizzle.parser.syntaxtree.TupleType;
import sizzle.parser.syntaxtree.Type;
import sizzle.parser.syntaxtree.TypeDecl;
import sizzle.parser.syntaxtree.VarDecl;
import sizzle.parser.syntaxtree.WhenStatement;
import sizzle.parser.syntaxtree.WhileStatement;
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
}
