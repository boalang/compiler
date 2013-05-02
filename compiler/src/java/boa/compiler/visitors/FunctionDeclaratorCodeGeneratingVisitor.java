package boa.compiler.visitors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import boa.compiler.TypeException;
import boa.compiler.ast.Component;
import boa.compiler.ast.types.FunctionType;
import boa.types.BoaAny;
import boa.types.BoaFunction;

/***
 * Finds the set of all function types and generates classes for each unique type.
 * 
 * @author rdyer
 */
public class FunctionDeclaratorCodeGeneratingVisitor extends AbstractCodeGeneratingVisitor {
	private final Set<String> funcs = new HashSet<String>();

	public FunctionDeclaratorCodeGeneratingVisitor(final StringTemplateGroup stg) {
		this.stg = stg;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionType n) {
		final String name = ((BoaFunction)n.type).toJavaType();
		if (funcs.contains(name))
			return;

		funcs.add(name);

		final StringTemplate st = this.stg.getInstanceOf("FunctionType");

		if (!(n.type instanceof BoaFunction))
			throw new TypeException(n ,"type " + n.type + " is not a function type");

		final BoaFunction funcType = ((BoaFunction) n.type);

		final List<Component> params = n.getArgs();
		final List<String> args = new ArrayList<String>();
		final List<String> types = new ArrayList<String>();

		for (final Component c : params) {
			args.add(c.getIdentifier().getToken());
			types.add(c.getType().type.toJavaType());
		}

		st.setAttribute("name", funcType.toJavaType());
		if (funcType.getType() instanceof BoaAny)
			st.setAttribute("ret", "void");
		else
			st.setAttribute("ret", funcType.getType().toBoxedJavaType());
		st.setAttribute("args", args);
		st.setAttribute("types", types);

		code.add(st.toString());
	}
}
