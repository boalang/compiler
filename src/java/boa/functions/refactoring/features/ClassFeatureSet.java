package boa.functions.refactoring.features;

import java.util.ArrayList;
import java.util.List;

import boa.functions.refactoring.BoaRefactoringPredictionIntrinsics;
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Ast.Method;
import boa.types.Ast.Variable;

public class ClassFeatureSet {
	public List<MethodFeatureSet> methodFeatureSets = new ArrayList<MethodFeatureSet>();
	public int nField = 0; // num of fields
	public int nASTNode = 0; // num of AST nodes
	public double[] metrics = null; // C&K metrics: wmc, rfc, lcom, dit, noc, cbo

	private class ASTCountVisitor extends BoaAbstractVisitor {
		int astCount = 0;

		protected boolean defaultPreVisit() throws Exception {
			astCount++;
			return true;
		}

		protected int getASTCount() {
			int res = astCount;
			astCount = 0;
			return res;
		}
	}

	private BoaAbstractVisitor classVisitor = new BoaAbstractVisitor() {
		@Override
		public boolean preVisit(final Declaration node) throws Exception {
			ASTCountVisitor astCounter = new ASTCountVisitor();
			for (Variable v : node.getFieldsList()) {
				nField++;
				astCounter.visit(v);
				nASTNode += astCounter.getASTCount();
			}
			for (Method m : node.getMethodsList()) {
				astCounter.visit(m);
				methodFeatureSets.add(new MethodFeatureSet(astCounter.astCount));
				nASTNode += astCounter.getASTCount();
			}
			return false;
		}
	};

	public ClassFeatureSet(Declaration node, double[] metrics) throws Exception {
		classVisitor.visit(node);
		this.metrics = metrics;
	}

	@Override
	public String toString() {
		return BoaRefactoringPredictionIntrinsics.gson.toJson(this);
	}
}