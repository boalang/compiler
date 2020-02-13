package boa.functions.refactoring.features;

import java.util.ArrayList;
import java.util.List;

import boa.functions.refactoring.BoaRefactoringPredictionIntrinsics;
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Ast.Method;
import boa.types.Ast.Variable;

import static boa.functions.refactoring.BoaRefactoringIntrinsics.getStats;

public class ClassFeatureSet {
	
	// ---- FEATURES start ------
	public int isTestClass = 0;
	public int nFieldInClass = 0; // num of fields
	public int nASTNodeInClass = 0; // num of AST nodes
	public List<MethodFeatureSet> methodFeatureSets = new ArrayList<MethodFeatureSet>();
	public double[] ckInClass = null; // C&K metrics: wmc, rfc, lcom, dit, noc, cbo
	
	// ---- statistics(min, max, mean, median, std) of the class
	double[] astStatsInFieldOfClass = null;
	double[] astStatsInMethodOfClass = null;
	// ---- FEATURES end ------
	
	private List<Integer> astNodeNumsInFieldOfClass = new ArrayList<Integer>();
	private List<Integer> astNodeNumsInMethodOfClass = new ArrayList<Integer>();

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
				nFieldInClass++;
				astCounter.visit(v);
				int ast = astCounter.getASTCount();
				astNodeNumsInFieldOfClass.add(ast);
				nASTNodeInClass += ast;
			}
			for (Method m : node.getMethodsList()) {
				astCounter.visit(m);
				getMethodFeatureSets().add(new MethodFeatureSet(astCounter.astCount));
				int ast = astCounter.getASTCount();
				astNodeNumsInMethodOfClass.add(ast);
				nASTNodeInClass += ast;
			}
			return false;
		}
	};

	public ClassFeatureSet(Declaration node, double[] metrics) throws Exception {
		classVisitor.visit(node);
		this.ckInClass = metrics;
		this.astStatsInFieldOfClass = getStats(astNodeNumsInFieldOfClass);
		this.astStatsInMethodOfClass = getStats(astNodeNumsInMethodOfClass);
	}

	@Override
	public String toString() {
		return BoaRefactoringPredictionIntrinsics.gson.toJson(this);
	}

	public List<MethodFeatureSet> getMethodFeatureSets() {
		return methodFeatureSets;
	}
}