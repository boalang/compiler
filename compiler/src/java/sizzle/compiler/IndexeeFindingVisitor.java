package sizzle.compiler;

import java.util.HashSet;
import java.util.Set;

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
import sizzle.parser.syntaxtree.Node;
import sizzle.parser.syntaxtree.NodeSequence;
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
				indexees.addAll(node.accept(this, argu));

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
		return n.f0.accept(this, argu);
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
}
