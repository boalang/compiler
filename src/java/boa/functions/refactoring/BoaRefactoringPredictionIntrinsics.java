package boa.functions.refactoring;

import static boa.functions.BoaIntrinsics.getRevisionById;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import boa.functions.FunctionSpec;
import boa.types.Code.Change;
import boa.types.Code.CodeRefactoring;
import boa.types.Code.CodeRepository;
import boa.types.Code.Location;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import boa.types.Toplevel.Project;

import static boa.functions.refactoring.BoaRefactoringIntrinsics.*;
import static boa.functions.BoaAstIntrinsics.*;
import static boa.functions.BoaIntrinsics.*;

public class BoaRefactoringPredictionIntrinsics {

	public static HashMap<String, Integer> DETECTED_TYPE_IDX_MAP = new HashMap<String, Integer>();
	public static HashMap<ChangeKind, Integer> FILE_CHANGE_IDX_MAP = new HashMap<ChangeKind, Integer>();
	public static HashSet<String> REFACTORING_COMMIT_IDS = null;

	static {
		for (int i = 0; i < DETECTED_TYPES.length; i++)
			DETECTED_TYPE_IDX_MAP.put(DETECTED_TYPES[i], i);
		ChangeKind[] kinds = new ChangeKind[] { ChangeKind.ADDED, ChangeKind.DELETED, ChangeKind.MODIFIED,
				ChangeKind.RENAMED, ChangeKind.COPIED };
		for (int i = 0; i < kinds.length; i++)
			FILE_CHANGE_IDX_MAP.put(kinds[i], i);
	}

	private static HashSet<String> getRefactoringIds(Project p) {
		if (REFACTORING_COMMIT_IDS == null)
			;
		REFACTORING_COMMIT_IDS = getRefactoringIdsInSet(p);
		return REFACTORING_COMMIT_IDS;
	}

	@FunctionSpec(name = "get_detected_types", returnType = "array of string")
	public static HashMap<String, Integer> getDetectedTypes() throws Exception {
		return DETECTED_TYPE_IDX_MAP;
	}

	@FunctionSpec(name = "print_ref_stat", formalParameters = { "Project" })
	public static void printRefStat(Project p) throws Exception {
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
//			List<CodeRefactoring> refs = getRefactorings(p, r);
			List<CodeRefactoring> refs = getCodeChange(p, r).getRefactoringsList();
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

	private static List<CodeRefactoring> getRefactorings(Project p, Revision r) {
		List<CodeRefactoring> refs = getCodeChange(p, r).getRefactoringsList();
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

	@FunctionSpec(name = "print_file_changes", formalParameters = { "Project" })
	public static void printFileChanges(Project p) throws Exception {
		HashMap<String, Integer[]> fileChanges = getFileChangeMap(p);
		for (Entry<String, Integer[]> entry : fileChanges.entrySet()) {
			System.out.println(entry.getKey() + " " + Arrays.toString(entry.getValue()));
		}
	}

	private static HashMap<String, Integer[]> getFileChangeMap(Project p) {
		CodeRepository cr = p.getCodeRepositories(0);
		int revCount = getRevisionsCount(cr);
		HashMap<String, Integer[]> fileChanges = new HashMap<String, Integer[]>();
		for (int i = 0; i < revCount; i++) {
			Revision r = getRevision(cr, i);
			for (ChangedFile cf : r.getFilesList()) {
				if (!isJavaFile(cf.getName()))
					continue;
				if (!fileChanges.containsKey(cf.getName()))
					fileChanges.put(cf.getName(),
							// ADDED, DELETED, MODIFIED, RENAMED, COPIED
							new Integer[] { 0, 0, 0, 0, 0 });
				fileChanges.get(cf.getName())[FILE_CHANGE_IDX_MAP.get(cf.getChange())] += 1;
			}
		}
		return fileChanges;
	}

	private static HashMap<String, Integer[]> getFileTypeMap(Project p) throws Exception {
		HashSet<String> refRevIds = getRefactoringIdsInSet(p);
		if (refRevIds.size() == 0)
			return new HashMap<String, Integer[]>();
		CodeRepository cr = p.getCodeRepositories(0);
		int revCount = getRevisionsCount(cr);
		HashMap<String, Integer[]> fileToRefTypes = new HashMap<String, Integer[]>();
		HashMap<String, Integer> typeToIdx = getDetectedTypes();
		for (int i = 0; i < revCount; i++) {
			Revision r = getRevision(cr, i);
			if (!refRevIds.contains(r.getId()))
				continue;
			List<CodeRefactoring> refs = getRefactorings(p, r);
			for (CodeRefactoring ref : refs) {
				String type = ref.getType();
				Location loc = ref.getLeftSideLocations(0);
				String leftFilePath = loc.getFilePath();
				if (!fileToRefTypes.containsKey(leftFilePath))
					fileToRefTypes.put(leftFilePath, new Integer[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
				int idx = typeToIdx.get(type);
				fileToRefTypes.get(leftFilePath)[idx] += 1;
			}
		}
		return fileToRefTypes;
	}

	@FunctionSpec(name = "print_file_before_change", formalParameters = { "Project" })
	public static void printFileBeforeChange(Project p) throws Exception {
		HashMap<String, Integer[]> fileChanges = getFileChangeMap(p);
		HashMap<String, Integer[]> fileToRefTypes = getFileTypeMap(p);
		for (Entry<String, Integer[]> entry : fileToRefTypes.entrySet()) {
			String filePath = entry.getKey();
			System.out.println(filePath + " " + Arrays.toString(entry.getValue()) + " "
					+ Arrays.toString(fileChanges.get(filePath)));
		}
	}

	@FunctionSpec(name = "test", formalParameters = { "Project" })
	public static void test(Project p) throws Exception {
		CodeRepository cr = p.getCodeRepositories(0);
		int revCount = getRevisionsCount(cr);
//		FileLinkedLists fileLinkLists = new FileLinkedLists();
		FileChangeLinkedLists fileLinkLists = new FileChangeLinkedLists();
		for (int i = 0; i < revCount; i++) {
			Revision r = getRevision(cr, i);
//			fileLinkLists.update(r.getFilesList(), r, getRefactorings(p, r));
			fileLinkLists.update(r.getFilesList(), r, getCodeChange(p, r).getRefactoringsList());
		}
		System.out.println(fileLinkLists.getLists().size());
		int count = 0;
		for (Entry<String, FileLinkedList> entry : fileLinkLists.getLists().entrySet()) {
			FileLinkedList list = entry.getValue();
			if (list.getFilesBeforeRefactoringMap().size() != 0) {
				count++;
//				System.out.println(entry.getKey() + " " + list.getChangeCountAsString() 
//						+ " " + list.getRefactoringTypeCountAsString());
			}
		}
		System.out.println(count);
	}

}
