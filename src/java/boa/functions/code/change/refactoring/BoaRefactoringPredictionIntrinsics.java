package boa.functions.code.change.refactoring;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import boa.datagen.util.FileIO;
import boa.functions.FunctionSpec;
import boa.functions.code.change.refactoring.features.RevisionFeatureSet;
import boa.types.Code.CodeRefactoring;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import boa.types.Toplevel.Project;

import static boa.functions.BoaAstIntrinsics.*;
import static boa.functions.BoaIntrinsics.*;
import static boa.functions.code.change.refactoring.BoaRefactoringIntrinsics.*;
import static boa.functions.code.change.refactoring.FileChangeLinkedLists.*;

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



	@FunctionSpec(name = "test2", formalParameters = { "Project" })
	public static void test2(Project p) throws Exception {
		CodeRepository cr = p.getCodeRepositories(0);
		HashSet<String> refRevIds = getRefactoringIds(p);
		if (refRevIds.size() == 0)
			return;
		int revCount = getRevisionsCount(cr);
		for (int i = 0; i < revCount; i++)
			getRev(cr, i);
		FileChangeLinkedLists cfLists = new FileChangeLinkedLists(false);
//		System.out.println(cfLists.validation());
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
		HashSet<Integer> observedRevIdx = cfLists.getObservedRevIdx();

		// print
		System.out.println("Total Revs: " + revCount);
		System.out.println("lists count: " + lists.size());
		System.out.println("ref lists count: " + refListIdxs.size());
		System.out.println("no ref lists count: " + noRefListIdxs.size());
		ChangedFile[] LatestSnapshot = getSnapshot(cr, revCount - 1, true);
		System.out.println("last snapshot size: " + LatestSnapshot.length);
		System.out.println("Observed ref files: " + refNodeLocs.size());
		System.out.println("Observed no ref files: " + noRefNodeLocs.size());
		System.out.println("Observed rev count: " + observedRevIdx.size());
		System.out.println();
		
		StringBuilder sb = new StringBuilder();
		int dataCount = 0;
		int obRevCount = 0;
		for (int revIdx : observedRevIdx) {
			obRevCount++;
			ChangedFile[] snapshot = getSnapshot(cr, revIdx, true);
			RevisionFeatureSet rfs = new RevisionFeatureSet(snapshot, getRev(cr, revIdx));
			for (String output : rfs.toOutputListsForBC(refNodeLocs, noRefNodeLocs)) {
				if (dataCount == 0)
					sb.append(colSB.toString() + "\n");
				sb.append(output + "\n");
				System.out.println(obRevCount + " " + revIdx + " " + ++dataCount);
//				System.out.println(output);
				break;
			}
			break;
		}
		
//		FileIO.writeFileContents(new File("/Users/hyj/git/BoaData/RefactoringAnalysis/Output2/prediction/ob3.txt"), sb.toString());

	}

	public static Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PRIVATE).setVersion(2.0)
			.setPrettyPrinting().create();
	// output string column names
	public static StringBuilder colSB = new StringBuilder();
	public static boolean updateCols1 = true;
	public static boolean updateCols2 = true;

	public static class ProjectFeatureSet {
		int projectType = -1; // 0 - user, 1 - org
	}

}
