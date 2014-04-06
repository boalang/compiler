package boa.compiler.visitors;

import java.util.LinkedList;
import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import boa.compiler.ast.Node;
import boa.compiler.ast.Pair;

/**
 * 
 * @author rdyer
 */
public abstract class AbstractCodeGeneratingVisitor extends AbstractVisitorNoArg {
	public static STGroup stg = new STGroupFile("templates/BoaJavaHadoop.stg");
	static {
		stg.importTemplates(new STGroupFile("templates/BoaJava.stg"));
	}

	protected final LinkedList<String> code = new LinkedList<String>();

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

		final ST st = stg.getInstanceOf("Pair");

		st.add("map", n.env.getId());
		st.add("key", code.pop());
		st.add("value", code.pop());

		code.add(st.render());
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
