package boa.compiler;

import java.util.HashSet;
import java.util.Set;

import boa.parser.syntaxtree.*;

public class IndexeeFindingVisitor extends DefaultVisitor<Set<String>, String> {
	private final CodeGeneratingVisitor codegen;
	private final NameFindingVisitor namefinder;
	private Factor last_factor;

	public IndexeeFindingVisitor(final CodeGeneratingVisitor codegen, final NameFindingVisitor namefinder) {
		this.codegen = codegen;
		this.namefinder = namefinder;
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
			set.add(last_factor.accept(codegen));
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
	public Set<String> visitOperandDollar(final Operand n, final String argu) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visitOperandFactor(final NodeToken op, final Factor n, final String argu) {
		return n.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visitOperandParen(final Expression n, final String argu) {
		return n.accept(this, argu);
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
