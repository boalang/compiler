package sizzle.compiler;

import java.util.HashSet;
import java.util.Set;

import sizzle.parser.syntaxtree.*;
import sizzle.parser.visitor.GJDepthFirst;

public class IndexeeFindingVisitor extends GJDepthFirst<Set<String>, String> {
	private final CodeGeneratingVisitor codegen;
	private SymbolTable symtab;
	private final NameFindingVisitor namefinder;
	private Factor last_factor;

	public IndexeeFindingVisitor(final CodeGeneratingVisitor codegen, final NameFindingVisitor namefinder) {
		this.codegen = codegen;
		this.namefinder = namefinder;
	}

	public void setSymbolTable(final SymbolTable symtab) {
		this.symtab = symtab;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Start n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Program n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Declaration n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final TypeDecl n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final StaticVarDecl n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final VarDecl n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Type n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Component n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ArrayType n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final TupleType n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final SimpleTupleType n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final SimpleMemberList n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final SimpleMember n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ProtoTupleType n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ProtoMemberList n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ProtoMember n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ProtoFieldDecl n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final MapType n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final OutputType n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ExprList n, final String argu) {
		final Set<String> indexees = new HashSet<String>();

		indexees.addAll(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(((NodeSequence)node).nodes.get(1).accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final FunctionType n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Statement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Assignment n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Block n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final BreakStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ContinueStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final DoStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final EmitStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ExprStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ForStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IfStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ResultStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ReturnStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final SwitchStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final WhenStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final WhenKind n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IdentifierList n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final WhileStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Expression n, final String argu) {
		final Set<String> indexees = new HashSet<String>();

		indexees.addAll(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(((NodeSequence)node).nodes.get(1).accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Conjunction n, final String argu) {
		final Set<String> indexees = new HashSet<String>();

		indexees.addAll(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(((NodeSequence)node).nodes.get(1).accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Comparison n, final String argu) {
		final Set<String> indexees = new HashSet<String>();

		indexees.addAll(n.f0.accept(this, argu));

		if (n.f1.present())
			indexees.addAll(((NodeSequence)n.f1.node).nodes.get(1).accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final SimpleExpr n, final String argu) {
		final Set<String> indexees = new HashSet<String>();

		indexees.addAll(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(node.accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Term n, final String argu) {
		final Set<String> indexees = new HashSet<String>();

		indexees.addAll(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(node.accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Factor n, final String argu) {
		final Set<String> indexees = new HashSet<String>();

		last_factor = n;

		indexees.addAll(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(node.accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Selector n, final String argu) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Index n, final String argu) {
		final HashSet<String> set = new HashSet<String>();

		if (this.namefinder.visit(n.f1).contains(argu)) {
			// FIXME rdyer
			codegen.setSkipIndex(argu);
			set.add(last_factor.accept(codegen, symtab));
			codegen.setSkipIndex("");
		}

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Call n, final String argu) {
		if (n.f1.present())
			return ((ExprList) n.f1.node).accept(this, argu);
		else
			return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final RegexpList n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Regexp n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Operand n, final String argu) {
		switch (n.f0.which) {
		case 0: // identifier
		case 1: // string literal
		case 2: // integer literal
		case 3: // floating point literal
		case 4: // composite
		case 5: // function
		case 8: // statement expression
			return n.f0.choice.accept(this, argu);
		case 6: // unary operator
		case 9: // parenthetical
			return ((NodeSequence)n.f0.choice).elementAt(1).accept(this, argu);
		default:
			throw new RuntimeException("unimplemented");
		}
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Composite n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final PairList n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Pair n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Function n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final StatementExpr n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Identifier n, final String argu) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IntegerLiteral n, final String argu) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final FingerprintLiteral n, final String argu) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final FloatingPointLiteral n, final String argu) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final CharLiteral n, final String argu) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final StringLiteral n, final String argu) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final BytesLiteral n, final String argu) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final TimeLiteral n, final String argu) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final EmptyStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final StopStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final VisitorExpr n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final VisitorType n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final VisitStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}
}
