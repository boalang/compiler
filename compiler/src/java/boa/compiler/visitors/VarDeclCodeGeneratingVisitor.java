package boa.compiler.visitors;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import boa.compiler.ast.statements.VarDeclStatement;
import boa.types.BoaTable;

/**
 * Scan the program and generate code for any variable declarations.
 * 
 * @author rdyer
 */
public class VarDeclCodeGeneratingVisitor extends AbstractCodeGeneratingVisitor {
	public VarDeclCodeGeneratingVisitor(final StringTemplateGroup stg) {
		this.stg = stg;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VarDeclStatement n) {
		if (n.type instanceof BoaTable)
			return;

		final StringTemplate st = this.stg.getInstanceOf("VarDecl");

		st.setAttribute("id", n.getId().getToken());
		st.setAttribute("type", n.type.toJavaType());

		if (n.isStatic())
			st.setAttribute("isstatic", true);

		code.add(st.toString());
	}
}
