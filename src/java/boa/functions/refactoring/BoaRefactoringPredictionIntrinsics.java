package boa.functions.refactoring;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import boa.functions.FunctionSpec;
import boa.functions.refactoring.features.ClassFeatureSet;
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Ast.Namespace;
import boa.types.Code.CodeRefactoring;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import boa.types.Toplevel.Project;
import javafx.scene.control.Tab;

import static boa.functions.refactoring.BoaRefactoringIntrinsics.*;
import static boa.functions.BoaAstIntrinsics.*;
import static boa.functions.BoaIntrinsics.*;
import static boa.functions.BoaMetricIntrinsics.getMetrics;

public class BoaRefactoringPredictionIntrinsics {

	public static HashMap<String, Integer> DETECTED_TYPE_IDX_MAP = new HashMap<String, Integer>();
	public static HashMap<ChangeKind, Integer> FILE_CHANGE_IDX_MAP = new HashMap<ChangeKind, Integer>();
	public static HashSet<String> REFACTORING_COMMIT_IDS = null;

	public static HashSet<String> DETECTED_REFACTORING_TYPES = new HashSet<String>();
	public static HashSet<String> CLASS_LEVEL_REFACTORING_TYPES = new HashSet<String>();
	public static HashSet<String> METHOD_LEVEL_REFACTORING_TYPES = new HashSet<String>();
	public static HashSet<String> FIELD_LEVEL_REFACTORING_TYPES = new HashSet<String>();

	static {
		for (int i = 0; i < DETECTED_TYPES.length; i++)
			DETECTED_TYPE_IDX_MAP.put(DETECTED_TYPES[i], i);
		ChangeKind[] kinds = new ChangeKind[] { ChangeKind.ADDED, ChangeKind.DELETED, ChangeKind.MODIFIED,
				ChangeKind.RENAMED, ChangeKind.COPIED };
		for (int i = 0; i < kinds.length; i++)
			FILE_CHANGE_IDX_MAP.put(kinds[i], i);
		String[] classLevel = new String[] { "Move Class", "Rename Class", "Extract Superclass", "Extract Interface" };
		String[] methodLevel = new String[] { "Rename Method", "Inline Method", "Extract Method",
				"Extract And Move Method", "Move Method", "Pull Up Method", "Push Down Method" };
		String[] fieldLevel = new String[] { "Move Attribute", "Pull Up Attribute", "Push Down Attribute" };
		for (String s : DETECTED_TYPES)
			DETECTED_REFACTORING_TYPES.add(s);
		for (String s : classLevel)
			CLASS_LEVEL_REFACTORING_TYPES.add(s);
		for (String s : methodLevel)
			METHOD_LEVEL_REFACTORING_TYPES.add(s);
		for (String s : fieldLevel)
			FIELD_LEVEL_REFACTORING_TYPES.add(s);
	}

	private static HashSet<String> getRefactoringIds(Project p) {
		if (REFACTORING_COMMIT_IDS == null)
			REFACTORING_COMMIT_IDS = getRefactoringIdsInSet(p);
		return REFACTORING_COMMIT_IDS;
	}

	@FunctionSpec(name = "get_detected_types", returnType = "array of string")
	public static HashMap<String, Integer> getDetectedTypes() throws Exception {
		return DETECTED_TYPE_IDX_MAP;
	}

	@FunctionSpec(name = "print_ref_stat", formalParameters = { "Project" })
	public static void printRefStat(Project p, Set<String> refTypes) throws Exception {
		HashMap<String, Integer> typeCounts = new HashMap<String, Integer>();
		for (int i = 0; i < DETECTED_TYPES.length; i++)
			typeCounts.put(DETECTED_TYPES[i], 0);
		CodeRepository cr = p.getCodeRepositories(0);
		int revCount = getRevisionsCount(cr);
		HashSet<String> refRevIds = getRefactoringIds(p);
		if (refRevIds.size() == 0)
			return;
		for (int i = 0; i < revCount; i++) {
			Revision r = getRevision(cr, i);
			if (!refRevIds.contains(r.getId()))
				continue;
			List<CodeRefactoring> refs = refTypes == null ? getCodeChange(p, r).getRefactoringsList()
					: getRefactorings(p, r, refTypes);
			for (CodeRefactoring ref : refs) {
				String type = ref.getType();
				if (typeCounts.containsKey(type)) {
					typeCounts.put(type, typeCounts.get(type) + 1);
				}
			}
		}
		for (Entry<String, Integer> entry : typeCounts.entrySet())
			System.out.println(entry.getKey() + " " + entry.getValue());
	}

	public static List<CodeRefactoring> getRefactorings(Project p, Revision r, Set<String> set) {
		List<CodeRefactoring> refs = getCodeChange(p, r).getRefactoringsList();
		refs = filterOutChangePackage(refs);
		refs = filterOutNestedClass(refs);
		List<CodeRefactoring> res = new ArrayList<CodeRefactoring>();
		for (CodeRefactoring ref : refs)
			if (set.contains(ref.getType()))
				res.add(ref);
		return res;
	}

	private static List<CodeRefactoring> filterOutNestedClass(List<CodeRefactoring> refs) {
		List<CodeRefactoring> res = new ArrayList<CodeRefactoring>();
		for (CodeRefactoring ref : refs) {
			if (CLASS_LEVEL_REFACTORING_TYPES.contains(ref.getType())) {
				String filePath = ref.getLeftSideLocations(0).getFilePath();
				String left = ref.getLeftSideLocations(0).getCodeElement();
				left = left.replace(".", "/");
				if (!filePath.contains(left)) {
					continue;
				}
			}
			res.add(ref);
		}
		return res;
	}

	private static List<CodeRefactoring> filterOutChangePackage(List<CodeRefactoring> refs) {
		// remove move class due to change package
		for (CodeRefactoring ref : refs)
			if (ref.getType().equals("Change Package")) {
				String leftPackage = getPackageFromFullyQualifiedName(ref.getLeftSideLocations(0).getCodeElement());
				String rightPackage = getPackageFromFullyQualifiedName(ref.getRightSideLocations(0).getCodeElement());
				List<CodeRefactoring> res = new ArrayList<CodeRefactoring>();
				for (CodeRefactoring refactoring : refs) {
					if (refactoring.getType().equals("Move Class")
							&& refactoring.getLeftSideLocations(0).getCodeElement().contains(leftPackage)
							&& refactoring.getRightSideLocations(0).getCodeElement().contains(rightPackage))
						continue;
					res.add(refactoring);
				}
				return res;
			}
		return refs;
	}

	private static String getPackageFromFullyQualifiedName(String fqn) {
		int idx = fqn.lastIndexOf('.');
		if (idx > 0)
			return fqn.substring(0, idx);
		return fqn;
	}

	public static HashMap<Integer, Rev> revIdxMap = new HashMap<Integer, Rev>();
	public static HashMap<String, Rev> revIdMap = new HashMap<String, Rev>();

	@FunctionSpec(name = "test2", formalParameters = { "Project" })
	public static void test2(Project p) throws Exception {
		CodeRepository cr = p.getCodeRepositories(0);
		HashSet<String> refRevIds = getRefactoringIds(p);
		if (refRevIds.size() == 0)
			return;
		int revCount = getRevisionsCount(cr);
		for (int i = revCount - 1; i >= 0; i--)
			getRev(cr, i);
		FileChangeLinkedLists cfLists = new FileChangeLinkedLists(false);
		List<FileChangeLinkedList> lists = cfLists.getLists();

		Set<String> refTypes = CLASS_LEVEL_REFACTORING_TYPES;
		refTypes.remove("Rename Class");
		refTypes.remove("Extract Superclass");
		refTypes.remove("Extract Interface");
//		printRefStat(p, null);

		cfLists.updateRefLists(p, refRevIds, refTypes);
		HashSet<Integer> refListIdxs = cfLists.getRefListIdxs();
		HashSet<Integer> noRefListIdxs = cfLists.getNoRefListIdxs();
		HashSet<String> refNodeLocs = cfLists.getRefNodeLocs();
		HashSet<String> noRefNodeLocs = cfLists.getNoRefNodeLocs();
		HashMap<Integer, List<FileNode>> revIdxToNodes = cfLists.getRevIdxToNodes();

//		for (Entry<Integer, List<FileNode>> entry : revIdxToNodes.entrySet()) {
//			int revIdx = entry.getKey();
//			List<FileNode> fileNodes = entry.getValue();
//
//			break;
//		}

		// print
		System.out.println("Total Revs: " + revCount);
		System.out.println("lists count: " + lists.size());
		System.out.println("ref lists count: " + refListIdxs.size());
		System.out.println("no ref lists count: " + noRefListIdxs.size());
		ChangedFile[] snapshot = getSnapshot(cr, revCount - 1, true);
		System.out.println("last snapshot size: " + snapshot.length);
		System.out.println("Observed ref files: " + refNodeLocs.size());
		System.out.println("Observed no ref files: " + noRefNodeLocs.size());
		System.out.println("Observed rev count: " + revIdxToNodes.size());

		RevisionFeatureSet cfs = new RevisionFeatureSet(snapshot);
		for (Entry<String, ClassFeatureSet> entry : cfs.classFeatures.entrySet()) {
			String key = entry.getKey();
			ClassFeatureSet c = entry.getValue();
		}
		System.out.println("\n" + cfs);
	}

	public static Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PRIVATE).setVersion(2.0)
			.setPrettyPrinting().create();

	public static class ProjectFeatureSet {
		int projectType = -1; // 0 - user, 1 - org
	}

	public static class RevisionFeatureSet {
		int nContributor = 0; // num of contributors
		int nPackage = 0; // num of packages
		int nFile = 0;
		int nClass = 0; // num of classes
		int nMethod = 0; // num of methods
		int nField = 0; // num of fields
		int nASTNode = 0; // num of AST nodes

		// ---- statistics(min, max, mean, median, std) of the entire snapshot
		// C&K metrics
		double[] wmcStats = null;
		double[] rfcStats = null;
		double[] lcomStats = null;
		double[] ditStats = null;
		double[] nocStats = null;
		double[] cboStats = null;

		double[] classDensity = null; // at package-level
		double[] methodDensity = null; // at class-level
		double[] fieldDensity = null; // at class-level
		double[] astDensityInClass = null; // at class-level
		double[] astDensityinMethod = null; // at method-level

		// ----
		private HashMap<String, double[]> metrics = null;
		HashMap<String, ClassFeatureSet> classFeatures = new HashMap<String, ClassFeatureSet>();

		private BoaAbstractVisitor fileVisitor = new BoaAbstractVisitor() {
			private List<Declaration> decls = new ArrayList<Declaration>();
			private String fileName = null;

			@Override
			public boolean preVisit(final ChangedFile cf) throws Exception {
				this.fileName = cf.getName();
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
				for (Declaration d : node.getDeclarationsList())
					visit(d); // collect all decls
				for (Declaration d : decls) {
					String classKey = fileName + " " + d.getFullyQualifiedName();
					ClassFeatureSet cfs = new ClassFeatureSet(d, metrics.get(classKey));
					classFeatures.put(classKey, cfs); // visit each decl
					nMethod += cfs.methodFeatureSets.size();
					nField += cfs.nField;
					nASTNode += cfs.nASTNode;
				}
				nClass += decls.size();
				decls.clear();
				return false;
			}
		};

		public RevisionFeatureSet(ChangedFile[] snapshot) throws Exception {
			this.metrics = getMetrics(snapshot);
			int count = 0;
			for (ChangedFile cf : snapshot) {
				nFile++;
				fileVisitor.visit(cf);
				if (count++ == 2) break;
			}
		}

		@Override
		public String toString() {
			return BoaRefactoringPredictionIntrinsics.gson.toJson(this);
		}
	}

	public static Rev getRev(CodeRepository cr, int idx) {
		if (revIdxMap.containsKey(idx))
			return revIdxMap.get(idx);
		Rev r = new Rev(idx, getRevision(cr, idx));
		revIdxMap.put(idx, r);
		revIdMap.put(r.rev.getId(), r);
		return revIdxMap.get(idx);
	}
}
