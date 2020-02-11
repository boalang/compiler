package boa.functions.refactoring;

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

import boa.functions.FunctionSpec;
import boa.functions.refactoring.BoaRefactoringPredictionIntrinsics.Rev;
import boa.functions.refactoring.FileChangeLinkedLists.FileChangeLinkedList;
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

	private static List<CodeRefactoring> getRefactorings(Project p, Revision r, Set<String> set) {
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
	
	private static HashMap<Integer, Rev> visited = new HashMap<Integer, Rev>();
	private static HashMap<String, Rev> revMap = new HashMap<String, Rev>();

	@FunctionSpec(name = "test2", formalParameters = { "Project" })
	public static void test2(Project p) throws Exception {
		CodeRepository cr = p.getCodeRepositories(0);
		int revCount = getRevisionsCount(cr);
		HashSet<String> refRevIds = getRefactoringIds(p);
		if (refRevIds.size() == 0)
			return;
		
		FileChangeLinkedLists cfLists = new FileChangeLinkedLists();
		for (int i = revCount - 1; i >= 0; i--) {
			Rev r = getRev(cr, i);
			cfLists.updateFileChangeLinkedLists(r);
		}
		HashMap<Integer, HashMap<Integer, List<Integer>>> links = cfLists.getLinks();
		List<FileChangeLinkedList> lists = cfLists.getLists();
		HashMap<String, Integer> fileLocIdToListIdx = cfLists.getFileLocIdToListIdxMap();
		
		// refactoring
		
//		Set<String> refTypes = CLASS_LEVEL_REFACTORING_TYPES;
//		refTypes.remove("Rename Class");
//		refTypes.remove("Extract Superclass");
//		refTypes.remove("Extract Interface");
//		printRefStat(p, null);
//		
//		for (String id : refRevIds) {
//			Rev r = revMap.get(id);
//			List<CodeRefactoring> refs = refTypes == null ? getCodeChange(p, r.rev).getRefactoringsList()
//					: getRefactorings(p, r.rev, refTypes);
//			for (CodeRefactoring ref : refs) {
//				String beforeFilePath = ref.getLeftSideLocations(0).getFilePath();
//				FileNode fn = findBeforeFile(beforeFilePath, r);
//				if (!fileLocIdToListIdx.containsKey(fn.getLocId()))
//					System.err.println("err 1");
//				int ListIdx = fileLocIdToListIdx.get(fn.getLocId());
//				FileChangeLinkedList list = lists.get(ListIdx);
//				if (list == null)
//					System.err.println("err 3");
//				if (!list.revIdxToNode.get(fn.revIdx).equals(fn))
//					System.err.println("err 4");
//				
//				list.refRevIdxs.add(fn.revIdx);
//			}
//		}
//		
//		List<FileChangeLinkedList> refLists = new ArrayList<FileChangeLinkedList>();
//		List<FileChangeLinkedList> noRefLists = new ArrayList<FileChangeLinkedList>();
//		for (FileChangeLinkedList list : lists)
//			if (list == null)
//				continue;
//			else if (list.refRevIdxs.size() > 0)
//				refLists.add(list);
//			else
//				noRefLists.add(list);
		
		
		
		// print
		System.out.println("Total Revs: " + revCount);
		for (int idx : links.keySet())
			System.out.println(visited.containsKey(idx));
		System.out.println("links count: " + links.size());
		System.out.println("lists total count: " + lists.size());
		int listCount = 0;
		for (FileChangeLinkedList list : lists) {
			if (list != null) listCount++;
		}
		System.out.println("lists count: " + listCount);
//		System.out.println("ref lists count: " + refLists.size());
//		System.out.println("no ref lists count: " + noRefLists.size());
		ChangedFile[] snapshot = getSnapshot(cr, revCount - 1, true);
		System.out.println("last snapshot size: " + snapshot.length);
	}

	private static FileNode findBeforeFile(String beforeFilePath, Rev r) {
		FileNode fn = null;
		boolean found = false;
		Rev cur = r;
		while (!found) {
			if (cur.rev.getParentsCount() == 0)
				System.err.println("err 2");
			Rev parent = visited.get(cur.rev.getParents(0));
			for (int i = 0; i < parent.rev.getFilesCount(); i++) {
				ChangedFile cf = parent.rev.getFiles(i);
				if (cf.getName().equals(beforeFilePath)) {
					fn = new FileNode(cf, parent.revIdx, i);
					found = true;
					break;
				}
			}
			cur = parent;
		}
		return fn;
	}

	public static Rev getRev(CodeRepository cr, int idx) {
		if (visited.containsKey(idx))
			return visited.get(idx);
		Rev r = new Rev(idx, getRevision(cr, idx));
		visited.put(idx, r);
		revMap.put(r.rev.getId(), r);
		return new Rev(idx, getRevision(cr, idx));
	}

	public static class Rev {
		int revIdx;
		Revision rev;

		public Rev(int revIdx, Revision rev) {
			this.revIdx = revIdx;
			this.rev = rev;
		}
	}
}
