package boa.compiler;

import boa.parser.syntaxtree.*;

public class StaticInitializationCodeGeneratingVisitor extends DefaultVisitorNoArgu<String> {
	private final CodeGeneratingVisitor codegenerator;

	public StaticInitializationCodeGeneratingVisitor(final CodeGeneratingVisitor codegenerator) {
		this.codegenerator = codegenerator;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Program n) {
		final StringBuilder sb = new StringBuilder();

		for (final Node node : n.f0.nodes) {
			final NodeChoice nodeChoice = (NodeChoice) node;
			switch (nodeChoice.which) {
			case 0: // declaration
				final String accept = nodeChoice.choice.accept(this);

				if (accept != null)
					sb.append(accept);
				break;
			case 1: // statement
				break;
			case 2: // proto
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		}

		return sb.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Declaration n) {
		switch (n.f0.which) {
		case 0: // type declaration
			return null;
		case 1: // static var declaration
			return n.f0.choice.accept(this);
		case 2: // variable declaration
			return null;
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final StaticVarDecl n) {
		return n.f1.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final VarDecl n) {
		if (n.f3.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f3.node;

			switch (nodeChoice.which) {
			case 0: // initializer
				final SymbolTable argu = this.codegenerator.typechecker.getSyms(n);
				argu.setId("___" + n.f0.f0.tokenImage);
				final String accept = ((NodeSequence) nodeChoice.choice).elementAt(1).accept(this);
				argu.setId(null);
				return accept;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		} else {
			return null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Expression n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Conjunction n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Comparison n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final SimpleExpr n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Term n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Factor n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visitOperandDollar(final Operand n) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String visitOperandFactor(final NodeToken op, final Factor n) {
		return n.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visitOperandParen(final Expression n) {
		return n.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Composite n) {
		if (n.f1.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f1.node;

			switch (nodeChoice.which) {
			case 0: // pair list
				return this.codegenerator.visit((PairList) nodeChoice.choice);
			case 1: // expression list
			case 2: // empty map
				return null;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		} else {
			throw new RuntimeException("unimplemented");
		}
	}
}
