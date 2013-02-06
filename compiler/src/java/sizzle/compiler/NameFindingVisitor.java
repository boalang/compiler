package sizzle.compiler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import sizzle.parser.syntaxtree.*;

public class NameFindingVisitor extends DefaultVisitorNoArgu<Set<String>> {
	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IdentifierList n) {
		final HashSet<String> set = new HashSet<String>();

		set.add(n.f0.f0.tokenImage);

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.addAll(((NodeSequence) node).elementAt(1).accept(this));

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Expression n) {
		final HashSet<String> set = new HashSet<String>();

		set.addAll(n.f0.accept(this));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.addAll(node.accept(this));

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Conjunction n) {
		final HashSet<String> set = new HashSet<String>();

		set.addAll(n.f0.accept(this));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.addAll(node.accept(this));

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Comparison n) {
		final HashSet<String> set = new HashSet<String>();

		set.addAll(n.f0.accept(this));

		if (n.f1.present())
			set.addAll(n.f1.node.accept(this));

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final SimpleExpr n) {
		final HashSet<String> set = new HashSet<String>();

		set.addAll(n.f0.accept(this));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.addAll(node.accept(this));

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Term n) {
		final HashSet<String> set = new HashSet<String>();

		set.addAll(n.f0.accept(this));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.addAll(node.accept(this));

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Factor n) {
		final HashSet<String> set = new HashSet<String>();

		set.addAll(n.f0.accept(this));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.addAll(node.accept(this));

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Selector n) {
		return new HashSet<String>(Arrays.asList(n.f1.f0.tokenImage));
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Index n) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visitOperandDollar(final Operand n) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visitOperandFactor(final NodeToken op, final Factor n) {
		return n.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visitOperandParen(final Expression n) {
		return n.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Identifier n) {
		return new HashSet<String>(Arrays.asList(n.f0.tokenImage));
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IntegerLiteral n) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final FingerprintLiteral n) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final FloatingPointLiteral n) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final CharLiteral n) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final StringLiteral n) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final BytesLiteral n) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final TimeLiteral n) {
		return new HashSet<String>();
	}
}
