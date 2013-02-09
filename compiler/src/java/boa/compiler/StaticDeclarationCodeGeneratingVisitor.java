package boa.compiler;

import java.io.IOException;

import org.antlr.stringtemplate.StringTemplate;

import boa.types.BoaTable;
import boa.types.BoaType;

import boa.parser.syntaxtree.*;

/**
 * Prescan the Boa program and generate initializer code for any static
 * variables.
 * 
 * @author anthonyu
 */
public class StaticDeclarationCodeGeneratingVisitor extends DefaultVisitorNoArgu<String> {
	private final CodeGeneratingVisitor codegenerator;

	public StaticDeclarationCodeGeneratingVisitor(final CodeGeneratingVisitor codegenerator) throws IOException {
		this.codegenerator = codegenerator;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Program n) {
		final StringBuilder sb = new StringBuilder();

		for (final Node node : n.f0.nodes) {
			final NodeChoice nodeChoice = (NodeChoice) node;
			switch (nodeChoice.which) {
			case 1: // statement
			case 0: // declaration
				final String accept = nodeChoice.choice.accept(this);

				if (accept != null)
					sb.append(accept);
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
		case 2: // variable declaration
			return n.f0.choice.accept(this);
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final StaticVarDecl n) {
		return "private static " + this.codegenerator.visit(n.f1);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final VarDecl n) {
		final BoaType type = this.codegenerator.typechecker.getBinding(n);

		if (type instanceof BoaTable)
			return null;

		final StringTemplate st = this.codegenerator.stg.getInstanceOf("VarDecl");

		st.setAttribute("id", n.f0.f0.tokenImage);
		st.setAttribute("type", type.toJavaType());

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Statement n) {
		switch (n.f0.which) {
		case 1:
		case 4:
		case 7:
		case 8:
		case 11:
		case 12:
		case 13:
		case 14:
			return n.f0.accept(this);
		default:
			return null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Block n) {
		final StringBuilder sb = new StringBuilder();

		for (final Node node : n.f1.nodes) {
			final String accept = node.accept(this);

			if (accept != null)
				sb.append(accept);
		}

		return sb.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final DoStatement n) {
		return n.f1.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final IfStatement n) {
		final String first = n.f4.accept(this);

		String rest = null;
		if (n.f5.present())
			rest = ((NodeSequence)n.f5.node).elementAt(1).accept(this);

		if (first == null)
			return rest;
		if (rest == null)
			return first;
		return first + "\n" + rest;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ForStatement n) {
		return n.f8.accept(this);
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
