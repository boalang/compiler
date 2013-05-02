package boa.compiler.visitors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import boa.compiler.TypeException;
import boa.compiler.ast.types.FunctionType;
import boa.types.BoaAny;
import boa.types.BoaFunction;
import boa.types.BoaName;
import boa.types.BoaType;

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

		final BoaType[] paramTypes = funcType.getFormalParameters();
		final List<String> args = new ArrayList<String>();
		final List<String> types = new ArrayList<String>();

		for (int i = 0; i < paramTypes.length; i++) {
			args.add(((BoaName) paramTypes[i]).getId());
			types.add(paramTypes[i].toJavaType());
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
