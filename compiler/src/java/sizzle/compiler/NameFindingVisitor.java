package sizzle.compiler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import sizzle.parser.syntaxtree.*;
import sizzle.parser.visitor.GJNoArguDepthFirst;

public class NameFindingVisitor extends GJNoArguDepthFirst<Set<String>> {

	@Override
	public Set<String> visit(final Start n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Program n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Declaration n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final TypeDecl n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final StaticVarDecl n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final VarDecl n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Type n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Component n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ArrayType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final TupleType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final SimpleTupleType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final SimpleMemberList n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final SimpleMember n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ProtoTupleType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ProtoMemberList n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ProtoMember n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ProtoFieldDecl n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final MapType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final OutputType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ExprList n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final FunctionType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Statement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Assignment n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Block n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final BreakStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ContinueStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final DoStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final EmitStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ExprStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ForStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final IfStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ResultStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ReturnStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final SwitchStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final WhenStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final IdentifierList n) {
		final HashSet<String> set = new HashSet<String>();

		set.add(n.f0.f0.tokenImage);

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.addAll(((NodeSequence) node).elementAt(1).accept(this));

		return set;
	}

	@Override
	public Set<String> visit(final WhileStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Expression n) {
		final HashSet<String> set = new HashSet<String>();

		set.addAll(n.f0.accept(this));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.addAll(node.accept(this));

		return set;
	}

	@Override
	public Set<String> visit(final Conjunction n) {
		final HashSet<String> set = new HashSet<String>();

		set.addAll(n.f0.accept(this));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.addAll(node.accept(this));

		return set;
	}

	@Override
	public Set<String> visit(final Comparison n) {
		final HashSet<String> set = new HashSet<String>();

		set.addAll(n.f0.accept(this));

		if (n.f1.present())
			set.addAll(n.f1.node.accept(this));

		return set;
	}

	@Override
	public Set<String> visit(final SimpleExpr n) {
		final HashSet<String> set = new HashSet<String>();

		set.addAll(n.f0.accept(this));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.addAll(node.accept(this));

		return set;
	}

	@Override
	public Set<String> visit(final Term n) {
		final HashSet<String> set = new HashSet<String>();

		set.addAll(n.f0.accept(this));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.addAll(node.accept(this));

		return set;
	}

	@Override
	public Set<String> visit(final Factor n) {
		final HashSet<String> set = new HashSet<String>();

		set.addAll(n.f0.accept(this));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.addAll(node.accept(this));

		return set;
	}

	@Override
	public Set<String> visit(final Selector n) {
		return new HashSet<String>(Arrays.asList(n.f1.f0.tokenImage));
	}

	@Override
	public Set<String> visit(final Index n) {
		return new HashSet<String>();
	}

	@Override
	public Set<String> visit(final Call n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final RegexpList n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Regexp n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Operand n) {
		switch (n.f0.which) {
		case 0: // identifier
			return n.f0.accept(this);
		case 1: // string literal
		case 2: // int literal
		case 3: // float literal
			return new HashSet<String>();
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	@Override
	public Set<String> visit(final Composite n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final PairList n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Pair n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Function n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final StatementExpr n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Identifier n) {
		return new HashSet<String>(Arrays.asList(n.f0.tokenImage));
	}

	@Override
	public Set<String> visit(final IntegerLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final FingerprintLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final FloatingPointLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final CharLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final StringLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final BytesLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final TimeLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final EmptyStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final StopStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final VisitorExpr n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final VisitorType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final VisitStatement n) {
		throw new RuntimeException("unimplemented");
	}
}
