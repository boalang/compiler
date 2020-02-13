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
	int nContributor = 0; // num of contributors
	int nPackage = 0; // num of packages
	int nFile = 0;
	int nClass = 0; // num of classes
	int nMethod = 0; // num of methods
	int nField = 0; // num of fields
	int nASTNode = 0; // num of AST nodes

	// ---- statistics(min, max, mean, median, std) of the entire snapshot
	double[][] ckStats = null; // C&K metrics - rows: wmc, rfc, lcom, dit, noc, cbo
	double[] classDensity = null; // at package-level
	double[] methodDensity = null; // at class-level
	double[] fieldDensity = null; // at class-level
	double[] astDensityInClass = null; // at class-level
	double[] astDensityinMethod = null; // at method-level
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
				if (isTestFile) cfs.isTestClass = 1;
				classFeatures.put(revIdx + " " + fileIdx, cfs); // visit each decl
				nMethod += cfs.methodFeatureSets.size();
				methodNums.add(cfs.methodFeatureSets.size());
				for (MethodFeatureSet mfs : cfs.methodFeatureSets)
					astNodeNumsInMethod.add(mfs.nASTNode);
				nField += cfs.nField;
				fieldNums.add(cfs.nField);
				nASTNode += cfs.nASTNode;
				astNodeNumsInClass.add(cfs.nASTNode);
			}
			nClass += decls.size();
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
			nFile++;
			fileVisitor.visit(cf);

			if (count++ == 1)
				break;
		}
		
		this.nContributor = rev.nContributorSoFar;
		this.nPackage = pkgToClassNum.size();
		this.classDensity = getStats(pkgToClassNum.values());
		this.methodDensity = getStats(methodNums);
		this.fieldDensity = getStats(fieldNums);
		this.astDensityInClass = getStats(astNodeNumsInClass);
		this.astDensityinMethod = getStats(astNodeNumsInMethod);
	}

	@Override
	public String toString() {
		return BoaRefactoringPredictionIntrinsics.gson.toJson(this);
	}
}