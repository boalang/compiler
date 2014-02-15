package boa.compiler.visitors;

import java.util.LinkedList;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import boa.compiler.ast.Node;
import boa.compiler.ast.Pair;

/**
 * 
 * @author rdyer
 */
public abstract class AbstractCodeGeneratingVisitor extends AbstractVisitorNoArg {
	protected final LinkedList<String> code = new LinkedList<String>();
	protected StringTemplateGroup stg;

	public String getCode() {
		String str = "";
		for (final String s : code)
			str += s;
		return str;
	}

	public boolean hasCode() {
		for (final String s : code)
			if (s.length() > 0)
				return true;
		return false;
	}

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		code.clear();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Pair n) {
		super.visit(n);

		final StringTemplate st = this.stg.getInstanceOf("Pair");

		st.setAttribute("map", n.env.getId());
		st.setAttribute("key", code.pop());
		st.setAttribute("value", code.pop());

		code.add(st.toString());
	}

	protected void visit(final List<? extends Node> nl) {
		String s = "";

		for (final Node n : nl) {
			n.accept(this);
			if (s.length() > 0)
				s += ", ";
			s += code.removeLast();
		}

		code.add(s);
	}
}
