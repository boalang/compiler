package boa.compiler;

import java.util.HashSet;
import java.util.Set;

import boa.types.BoaFunction;
import boa.types.BoaType;

import boa.parser.syntaxtree.*;

/***
 * Finds the set of all function types and generates classes for each unique type.
 * 
 * @author rdyer
 */
public class FunctionDeclaratorVisitor extends DefaultVisitorNoArgu<String> {
	private final CodeGeneratingVisitor codegenerator;
	private final Set<String> funcs = new HashSet<String>();

	public FunctionDeclaratorVisitor(final CodeGeneratingVisitor codegenerator) {
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
			case 1: // statement
				final String accept = nodeChoice.choice.accept(this);

				if (accept != null)
					sb.append(accept);
				break;
			case 2: // proto
				break;
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
		case 2: // variable declaration
			return n.f0.choice.accept(this);
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
		String rhs = null;

		if (n.f3.present()) {
			final NodeChoice c = (NodeChoice)n.f3.node;
			if (c.which == 0)
				rhs = ((NodeSequence)c.choice).elementAt(1).accept(this);
			else
				rhs = c.choice.accept(this);
		}

		if (n.f2.present()) {
			final String s = n.f2.node.accept(this);
			if (rhs == null)
				return s;
			return s + rhs;
		}

		return rhs;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Type n) {
		if (n.f0.which != 5)
			return null;
		return n.f0.choice.accept(this.codegenerator);
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
	public String visit(final Operand n) {
		if (n.f0.which != 5 && n.f0.which != 6 && n.f0.which != 9)
			return null;

		return n.f0.choice.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final FunctionType n) {
		final BoaType t = this.codegenerator.typechecker.getBinding(n);
		final String name = ((BoaFunction)t).toJavaType();
		if (funcs.contains(name))
			return null;

		funcs.add(name);
		return n.accept(this.codegenerator);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final VisitorExpr n) {
		return n.f1.accept(this);
	}

	@Override
	public String visit(StatementExpr n) {
		return n.f1.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Function n) {
		final String body = n.f1.accept(this);
		if (body != null)
			return n.f0.accept(this) + body;
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Statement n) {
		switch (n.f0.which) {
		case 0: // assignment
		case 1: // block
		case 4: // do
		case 6: // expr
		case 7: // for
		case 8: // if
		case 11: // switch
		case 12: // when
		case 13: // while
		case 14: // visit
			return n.f0.choice.accept(this);
		case 2: // break
		case 3: // continue
		case 5: // emit
		case 9: // result
		case 10: // return
		case 15: // stop
		case 16: // empty
			return null;
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Assignment n) {
		return n.f2.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Block n) {
		if (n.f1.present()) {
			final StringBuilder sb = new StringBuilder();

			for (final Node s : n.f1.nodes) {
				final String accept = ((NodeChoice)s).choice.accept(this);

				if (accept != null)
					sb.append(accept);
			}

			if (sb.length() > 0)
				return sb.toString();
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final DoStatement n) {
		return n.f1.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ExprStatement n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ForStatement n) {
		return n.f8.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final IfStatement n) {
		if (n.f5.present()) {
			final String body = ((NodeSequence)n.f5.node).elementAt(1).accept(this);
			if (body != null)
				return n.f4.accept(this) + body;
		}
		return n.f4.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final SwitchStatement n) {
		// TODO Auto-generated method stub
		return super.visit(n);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final WhenStatement n) {
		return n.f8.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final WhileStatement n) {
		return n.f4.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final VisitStatement n) {
		return n.f3.accept(this);
	}
}
