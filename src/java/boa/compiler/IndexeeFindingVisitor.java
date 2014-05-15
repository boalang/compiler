package boa.compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import boa.parser.syntaxtree.*;

public class IndexeeFindingVisitor extends DefaultVisitor<Set<Node>, String> {
	private final NameFindingVisitor namefinder;
	private Factor firstFactor;
	private Node lastFactor;
	public Map<Node, Node> lastFactors = new HashMap<Node, Node>();

	public IndexeeFindingVisitor(final NameFindingVisitor namefinder) {
		this.namefinder = namefinder;
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final ExprList n, final String argu) {
		final Set<Node> indexees = new HashSet<Node>();

		indexees.addAll(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(((NodeSequence)node).nodes.get(1).accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final Expression n, final String argu) {
		final Set<Node> indexees = new HashSet<Node>();

		indexees.addAll(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(((NodeSequence)node).nodes.get(1).accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final Conjunction n, final String argu) {
		final Set<Node> indexees = new HashSet<Node>();

		indexees.addAll(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(((NodeSequence)node).nodes.get(1).accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final Comparison n, final String argu) {
		final Set<Node> indexees = new HashSet<Node>();

		indexees.addAll(n.f0.accept(this, argu));

		if (n.f1.present())
			indexees.addAll(((NodeSequence)n.f1.node).nodes.get(1).accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final SimpleExpr n, final String argu) {
		final Set<Node> indexees = new HashSet<Node>();

		indexees.addAll(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(node.accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final Term n, final String argu) {
		final Set<Node> indexees = new HashSet<Node>();

		indexees.addAll(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(node.accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final Factor n, final String argu) {
		final Set<Node> indexees = new HashSet<Node>();

		firstFactor = n;
		lastFactor = n.f0;

		indexees.addAll(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes) {
				indexees.addAll(node.accept(this, argu));
				lastFactor = node;
			}

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final Selector n, final String argu) {
		return new HashSet<Node>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final Index n, final String argu) {
		final HashSet<Node> set = new HashSet<Node>();

		if (this.namefinder.visit(n).contains(argu)) {
			set.add(firstFactor);
			lastFactors.put(firstFactor, lastFactor);
		}

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final Call n, final String argu) {
		if (n.f1.present())
			return ((ExprList) n.f1.node).accept(this, argu);
		else
			return new HashSet<Node>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visitOperandDollar(final Operand n, final String argu) {
		return new HashSet<Node>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visitOperandFactor(final NodeToken op, final Factor n, final String argu) {
		return n.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visitOperandParen(final Expression n, final String argu) {
		return n.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final Identifier n, final String argu) {
		return new HashSet<Node>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final IntegerLiteral n, final String argu) {
		return new HashSet<Node>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final FingerprintLiteral n, final String argu) {
		return new HashSet<Node>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final FloatingPointLiteral n, final String argu) {
		return new HashSet<Node>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final CharLiteral n, final String argu) {
		return new HashSet<Node>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final StringLiteral n, final String argu) {
		return new HashSet<Node>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final BytesLiteral n, final String argu) {
		return new HashSet<Node>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<Node> visit(final TimeLiteral n, final String argu) {
		return new HashSet<Node>();
	}
}
