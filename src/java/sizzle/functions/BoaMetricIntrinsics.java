package sizzle.functions;

import sizzle.types.Ast.Declaration;
import sizzle.types.Ast.Statement.StatementKind;

/**
 * Boa domain-specific functions for computing software engineering metrics.
 * 
 * @author rdyer
 */
public class BoaMetricIntrinsics {
	/**
	 * Computes the Number of Attributes (NOA) metric for a Declaration.
	 * 
	 * @param decl the Declaration to compute NOA for
	 * @return the NOA value for decl
	 */
	@FunctionSpec(name = "get_metric_noa", returnType = "int", formalParameters = { "Declaration" })
	public static int getMetricNOA(final Declaration decl) {
	    int count = decl.getFieldsCount();

	    for (int i = 0; i < decl.getMethodsCount(); i++)
		    for (int j = 0; j < decl.getMethods(i).getStatementsCount(); j++)
		    	if (decl.getMethods(i).getStatements(j).getKind() == StatementKind.TYPEDECL)
		    		count += getMetricNOA(decl.getMethods(i).getStatements(j).getTypeDeclaration());

	    for (int i = 0; i < decl.getNestedDeclarationsCount(); i++)
	        count += getMetricNOA(decl.getNestedDeclarations(i));

	    return count;
	}

	/**
	 * Computes the Number of Operations (NOO) metric for a Declaration.
	 * 
	 * @param decl the Declaration to compute NOO for
	 * @return the NOO value for decl
	 */
	@FunctionSpec(name = "get_metric_noo", returnType = "int", formalParameters = { "Declaration" })
	public static int getMetricNOO(final Declaration decl) {
	    int count = decl.getMethodsCount();

	    for (int i = 0; i < decl.getMethodsCount(); i++)
		    for (int j = 0; j < decl.getMethods(i).getStatementsCount(); j++)
		    	if (decl.getMethods(i).getStatements(j).getKind() == StatementKind.TYPEDECL)
		    		count += getMetricNOO(decl.getMethods(i).getStatements(j).getTypeDeclaration());

	    for (int i = 0; i < decl.getNestedDeclarationsCount(); i++)
	        count += getMetricNOO(decl.getNestedDeclarations(i));

	    return count;
	}

	/**
	 * Computes the Number of Public Methods (NPM) metric for a Declaration.
	 * 
	 * @param decl the Declaration to compute NPM for
	 * @return the NPM value for decl
	 */
	@FunctionSpec(name = "get_metric_npm", returnType = "int", formalParameters = { "Declaration" })
	public static int getMetricNPM(final Declaration decl) {
	    int count = 0;

	    for (int i = 0; i < decl.getMethodsCount(); i++) {
		    for (int j = 0; j < decl.getMethods(i).getStatementsCount(); j++)
		    	if (decl.getMethods(i).getStatements(j).getKind() == StatementKind.TYPEDECL)
		    		count += getMetricNPM(decl.getMethods(i).getStatements(j).getTypeDeclaration());

    		if (BoaModifierIntrinsics.hasModifierPublic(decl.getMethods(i)))
	            count++;
    	}

	    for (int i = 0; i < decl.getNestedDeclarationsCount(); i++)
	        count += getMetricNPM(decl.getNestedDeclarations(i));

	    return count;
	}
}
