package boa.functions.refactoring.features;

import static boa.functions.BoaMetricIntrinsics.getMetrics;
import static boa.functions.refactoring.BoaRefactoringIntrinsics.getCKStats;
import static boa.functions.refactoring.BoaRefactoringIntrinsics.getPackageNameFromFQN;
import static boa.functions.refactoring.BoaRefactoringIntrinsics.getStats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import boa.functions.refactoring.BoaRefactoringPredictionIntrinsics;
import boa.functions.refactoring.Rev;
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Ast.Namespace;
import boa.types.Diff.ChangedFile;

public class RevisionFeatureSet {

	// ---- FEATURES start ------
	int nContributorInSnapshot = 0; // num of contributors
	int nPackageInSnapshot = 0; // num of packages
	int nFileInSnapshot = 0;
	int nClassInSnapshot = 0; // num of classes
	int nMethodInSnapshot = 0; // num of methods
	int nFieldInSnapshot = 0; // num of fields
	int nASTNodeInSnapshot = 0; // num of AST nodes

	// ---- statistics(min, max, mean, median, std) of the entire snapshot
	double[][] ckStats = null; // C&K metrics - rows: wmc, rfc, lcom, dit, noc, cbo
	double[] classStatsInPacakge = null; // at package-level
	double[] methodStatsInClass = null; // at class-level
	double[] fieldStatsInClass = null; // at class-level
	double[] astStatsInClass = null; // at class-level
	double[] astStatsInMethod = null; // at method-level
	// ---- FEATURES end ------

	private HashMap<String, double[]> metrics = null;
	 HashMap<String, ClassFeatureSet> classFeatures = new HashMap<String, ClassFeatureSet>();
	private HashMap<String, Integer> pkgToClassNum = new HashMap<String, Integer>();
	private List<Integer> methodNums = new ArrayList<Integer>();
	private List<Integer> fieldNums = new ArrayList<Integer>();
	private List<Integer> astNodeNumsInClass = new ArrayList<Integer>();
	private List<Integer> astNodeNumsInMethod = new ArrayList<Integer>();

	private BoaAbstractVisitor fileVisitor = new BoaAbstractVisitor() {
		private List<Declaration> decls = new ArrayList<Declaration>();
		private String fileName = null;
		private int revIdx = -1;
		private int fileIdx = -1;

		@Override
		public boolean preVisit(final ChangedFile cf) throws Exception {
			this.fileName = cf.getName();
			this.revIdx = cf.getRevisionIdx();
			this.fileIdx = cf.getFileIdx();
			return true;
		}

		@Override
		public boolean preVisit(final Declaration node) throws Exception {
			decls.add(node);
			for (Declaration d : node.getNestedDeclarationsList())
				visit(d);
			return false;
		}

		@Override
		public boolean preVisit(final Namespace node) throws Exception {
			if (!pkgToClassNum.containsKey(node.getName()))
				pkgToClassNum.put(node.getName(), 0);
			pkgToClassNum.put(node.getName(), pkgToClassNum.get(node.getName()) + 1);

			boolean isTestFile = false;
			for (String importLib : node.getImportsList())
				if (importLib.contains("junit")) {
					isTestFile = true;
					break;
				}

			for (Declaration d : node.getDeclarationsList())
				visit(d); // collect all decls
			for (Declaration d : decls) {
				String classKey = fileName + " " + d.getFullyQualifiedName();
				ClassFeatureSet cfs = new ClassFeatureSet(d, metrics.get(classKey));
				if (isTestFile)
					cfs.isTestClass = 1;
				classFeatures.put(revIdx + " " + fileIdx, cfs); // visit each decl
				nMethodInSnapshot += cfs.getMethodFeatureSets().size();
				methodNums.add(cfs.getMethodFeatureSets().size());
				for (MethodFeatureSet mfs : cfs.getMethodFeatureSets())
					astNodeNumsInMethod.add(mfs.nASTNodeInMethod);
				nFieldInSnapshot += cfs.nFieldInClass;
				fieldNums.add(cfs.nFieldInClass);
				nASTNodeInSnapshot += cfs.nASTNodeInClass;
				astNodeNumsInClass.add(cfs.nASTNodeInClass);
			}
			nClassInSnapshot += decls.size();
			decls.clear();
			fileName = null;
			return false;
		}
	};

	public RevisionFeatureSet(ChangedFile[] snapshot, Rev rev) throws Exception {
		this.metrics = getMetrics(snapshot);
		this.ckStats = getCKStats(metrics);

		int count = 0;
		for (ChangedFile cf : snapshot) {
			nFileInSnapshot++;
			fileVisitor.visit(cf);

			if (count++ == 0)
				break;
		}

		this.nContributorInSnapshot = rev.nContributorSoFar;
		this.nPackageInSnapshot = pkgToClassNum.size();
		this.classStatsInPacakge = getStats(pkgToClassNum.values());
		this.methodStatsInClass = getStats(methodNums);
		this.fieldStatsInClass = getStats(fieldNums);
		this.astStatsInClass = getStats(astNodeNumsInClass);
		this.astStatsInMethod = getStats(astNodeNumsInMethod);
	}
	
	public List<String> toOutputLists() {
		List<String> outputs = new ArrayList<String>();
		
		return outputs;
	}

	@Override
	public String toString() {
		return BoaRefactoringPredictionIntrinsics.gson.toJson(this);
	}
}