package boa.compiler;

import boa.parser.syntaxtree.*;

/**
 * Analyze the code to see if it is simple (no visitors) or not.
 * 
 * @author rdyer
 */
public class SimpleTaskVisitor extends DefaultVisitorNoArgu<Boolean> {
	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Program n) {
		for (final Node node : n.f0.nodes)
			if (!node.accept(this))
				return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Declaration n) {
		switch (n.f0.which) {
		case 1: // static variable declaration
		case 2: // variable declaration
			return n.f0.choice.accept(this);
		case 0: // type declaration
		default:
			return true;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final StaticVarDecl n) {
		return n.f1.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final VarDecl n) {
		if (n.f3.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f3.node;
			switch (nodeChoice.which) {
			case 0: // initializer
				return ((NodeSequence)nodeChoice.choice).elementAt(1).accept(this);
			case 1: // block
				return nodeChoice.choice.accept(this);
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		}

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Type n) {
		switch (n.f0.which) {
		case 0: // identifier
		case 1: // array
		case 2: // map
		case 3: // tuple
		case 4: // table
		case 5: // function
		case 7: // proto type
		case 8: // stack
		default:
			return true;
		case 6: // visitor
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Statement n) {
		switch (n.f0.which) {
		case 0: // assignment
		case 1: // block
		case 4: // do
		case 6: // exprstmt
		case 7: // for
		case 8: // if
		case 11: // switch
		case 12: // when
		case 13: // while
			return n.f0.choice.accept(this);
		case 14:
			return false;
		default:
			return true;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(Assignment n) {
		return n.f2.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(Block n) {
		if (n.f1.present())
			for (final Node c : n.f1.nodes)
				if (!((NodeChoice)c).choice.accept(this))
					return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(DoStatement n) {
		return n.f1.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(ExprStatement n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(ForStatement n) {
		return n.f8.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(IfStatement n) {
		if (n.f5.present())
			if (!((NodeSequence)n.f5.node).elementAt(1).accept(this))
				return false;
		return n.f4.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(SwitchStatement n) {
		if (n.f5.present())
			for (final Node ns : n.f5.nodes)
				if (!((NodeSequence)ns).elementAt(4).accept(this)) {
					return false;
				} else {
					NodeListOptional nlo = (NodeListOptional)((NodeSequence)ns).elementAt(5);
					if (nlo.present())
						for (final Node c : nlo.nodes)
							if (!c.accept(this))
								return false;
				}
		if (n.f9.present())
			for (final Node c : n.f9.nodes)
				if (!c.accept(this))
					return false;
		return n.f8.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(WhenStatement n) {
		return n.f8.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(WhileStatement n) {
		return n.f4.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Expression n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Conjunction n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Comparison n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final SimpleExpr n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Term n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Factor n) {
		if (n.f1.present())
			for (final Node nc : n.f1.nodes)
			switch (((NodeChoice)nc).which) {
			case 2:
				return ((NodeChoice)nc).choice.accept(this);
			default:
				return true;
			}
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(Call n) {
		if (n.f1.present())
			return n.f1.node.accept(this);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(ExprList n) {
		if (n.f1.present())
			for (final Node ns : n.f1.nodes)
				if (!((NodeSequence)ns).elementAt(1).accept(this))
					return false;
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(final Operand n) {
		switch (n.f0.which) {
		case 5: // visitor
			return false;
		case 6: // function
		case 9: // stmtexpr
			return n.f0.choice.accept(this);
		default:
			return true;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(Function n) {
		return n.f1.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(StatementExpr n) {
		return n.f1.accept(this);
	}
}
