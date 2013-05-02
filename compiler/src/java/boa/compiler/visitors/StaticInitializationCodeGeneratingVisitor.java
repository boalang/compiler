package boa.compiler.visitors;

import org.antlr.stringtemplate.StringTemplateGroup;

import boa.compiler.ast.Composite;
import boa.compiler.ast.Pair;
import boa.compiler.ast.statements.VarDeclStatement;

/**
 * 
 * @author rdyer
 */
public class StaticInitializationCodeGeneratingVisitor extends AbstractCodeGeneratingVisitor {
	public StaticInitializationCodeGeneratingVisitor(final StringTemplateGroup stg) {
		this.stg = stg;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VarDeclStatement n) {
		if (!n.isStatic() || !n.hasInitializer())
			return;

		n.env.setId("___" + n.getId().getToken());
		n.getInitializer().accept(this);
		n.env.setId(null);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Composite n) {
		if (n.isEmpty())
			return;

		String s = "";

		for (final Pair p : n.getPairs()) {
			if (s.length() > 0)
				s += "\n";
			p.accept(this);
			s += code.pop();
		}

		code.add(s);
	}
}
