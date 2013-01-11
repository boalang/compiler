package sizzle.functions;

import sizzle.types.Ast.*;

/**
 * Boa domain-specific functions for computing software engineering metrics.
 * 
 * @author rdyer
 */
public class BoaMetricIntrinsics {
	////////////////////////////////
	// Number of Attributes (NOA) //
	////////////////////////////////

	private static class BoaNOAVisitor extends BoaCountingVisitor {
		@Override
		public boolean preVisit(final Declaration node) {
			if (node.getKind() == TypeKind.CLASS)
				count += node.getFieldsCount();
			return true;
		}
	}
	private static BoaNOAVisitor noaVisitor = new BoaNOAVisitor();

	/**
	 * Computes the Number of Attributes (NOA) metric for a node.
	 * 
	 * @param node the node to compute NOA for
	 * @return the NOA value for decl
	 */
	@FunctionSpec(name = "get_metric_noa", returnType = "int", formalParameters = { "Declaration" })
	public static long getMetricNOA(final Declaration node) {
		noaVisitor.initialize().visit(node);
		return noaVisitor.count;
	}

	////////////////////////////////
	// Number of Operations (NOO) //
	////////////////////////////////

	private static class BoaNOOVisitor extends BoaCountingVisitor {
		@Override
		public boolean preVisit(final Declaration node) {
			if (node.getKind() == TypeKind.CLASS)
				count += node.getMethodsCount();
    		return true;
		}
	}
	private static BoaNOOVisitor nooVisitor = new BoaNOOVisitor();

	/**
	 * Computes the Number of Operations (NOO) metric for a node.
	 * 
	 * @param node the node to compute NOO for
	 * @return the NOO value for decl
	 */
	@FunctionSpec(name = "get_metric_noo", returnType = "int", formalParameters = { "Declaration" })
	public static long getMetricNOO(final Declaration node) {
		nooVisitor.initialize().visit(node);
		return nooVisitor.count;
	}

	////////////////////////////////////
	// Number of Public Methods (NPM) //
	////////////////////////////////////

	private static class BoaNPMVisitor extends BoaCountingVisitor {
		@Override
		public boolean preVisit(final Method node) {
    		if (BoaModifierIntrinsics.hasModifierPublic(node))
    			count++;
    		return true;
		}
	}
	private static BoaNPMVisitor npmVisitor = new BoaNPMVisitor();

	/**
	 * Computes the Number of Public Methods (NPM) metric for a Declaration.
	 * 
	 * @param decl the Declaration to compute NPM for
	 * @return the NPM value for decl
	 */
	@FunctionSpec(name = "get_metric_npm", returnType = "int", formalParameters = { "Declaration" })
	public static long getMetricNPM(final Declaration decl) {
		npmVisitor.initialize().visit(decl);
		return npmVisitor.count;
	}
}
