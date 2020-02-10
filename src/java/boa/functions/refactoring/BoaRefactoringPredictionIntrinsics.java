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
	private static List<FileChangeLinkedList> lists = new ArrayList<FileChangeLinkedList>();
	private static HashMap<String, Integer> fileLocIdToListIdx = new HashMap<String, Integer>();
	// rev idx to file idx to list idx
	private static HashMap<Integer, HashMap<Integer, List<Integer>>> links = new HashMap<Integer, HashMap<Integer, List<Integer>>>();

	@FunctionSpec(name = "test2", formalParameters = { "Project" })
	public static void test2(Project p) throws Exception {
		CodeRepository cr = p.getCodeRepositories(0);
		int revCount = getRevisionsCount(cr);
		System.out.println(revCount);
		int count = 0;
		for (int i = revCount - 1; i >= 0; i--) {
			Rev r = getRev(cr, i);
			updateFileChangeLinkedLists(r);
			count++;
		}
		
		System.out.println("Total Revs: " + count);
		for (int idx : links.keySet())
			System.out.println(visited.containsKey(idx));
		System.out.println("links count: " + links.size());
		System.out.println("lists total count: " + lists.size());
		int listCount = 0;
		for (FileChangeLinkedList list : lists) {
			if (list != null) listCount++;
		}
		System.out.println("lists count: " + listCount);
		
		int deletedFileCount = 0;
		ChangedFile[] snapshot = getSnapshot(cr, revCount - 1, true);
		for (ChangedFile cf : snapshot)
			if (cf.getChange() == ChangeKind.DELETED)
				deletedFileCount++;
		System.out.println("last snapshot size: " + snapshot.length);
		System.out.println("last snapshot deleted file count: " + deletedFileCount);
	}

	private static void updateFileChangeLinkedLists(Rev r) {
		// linking requests
		HashMap<Integer, ChangedFile> files = getFilesFrom(r.rev);
		if (links.containsKey(r.revIdx)) {
			for (Entry<Integer, List<Integer>> entry : links.remove(r.revIdx).entrySet()) {
				int fileIdx = entry.getKey();
				List<Integer> listIdxs = entry.getValue();
				ChangedFile cf = files.remove(fileIdx);

				FileChangeLinkedList list = null;
				if (listIdxs.size() > 1) {
					System.out.println("merge " + listIdxs.size());
					// merge other lists to the oldest list 
					Collections.sort(listIdxs);
					list = lists.get(listIdxs.get(0));
					for (int i = 1; i < listIdxs.size(); i++)
						list.merge(lists.get(listIdxs.get(i)));
				} else if (listIdxs.size() == 1) {
					list = lists.get(listIdxs.get(0));
				} else {
					System.err.println("err 1");
				}
				if (list != null) {
					FileNode node = new FileNode(cf, r.revIdx, fileIdx);
					if (list.getPrevFileIdx() != fileIdx && list.getPrevRevIdx() != r.revIdx)
						System.err.println("err 2");
					list.add(node);
				}
			}
		}
		// update files
		for (Entry<Integer, ChangedFile> entry : files.entrySet()) {
			ChangedFile cf = entry.getValue();
			if (isJavaFile(cf.getName())) {
				FileNode node = new FileNode(cf, r.revIdx, entry.getKey());
				int listId = lists.size();
				lists.add(new FileChangeLinkedList(node, listId));
			}
		}
	}

	private static HashMap<Integer, ChangedFile> getFilesFrom(Revision rev) {
		HashMap<Integer, ChangedFile> map = new HashMap<Integer, ChangedFile>();
		for (int i = 0; i < rev.getFilesCount(); i++)
			map.put(i, rev.getFiles(i));
		return map;
	}

	public static Rev getRev(CodeRepository cr, int idx) {
		if (visited.containsKey(idx))
			return visited.get(idx);
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

	public static class FileChangeLinkedList {
		public int id;
		public HashSet<String> fileLocIds = new HashSet<String>();
		public TreeMap<Integer, FileNode> revIdxToNode = new TreeMap<Integer, FileNode>();
		public int prevRevIdx = -1;
		public int prevFileIdx = -1;

		public FileChangeLinkedList(FileNode node, int listIdx) {
			this.id = listIdx;
			add(node);
		}

		public void add(FileNode node) {
			// check local set
			if (fileLocIds.contains(node.getLocId()))
				System.err.println(" fileLocIds err ");
			fileLocIds.add(node.getLocId());
			revIdxToNode.put(node.revIdx, node);
			// check global loc id set
			if (fileLocIdToListIdx.containsKey(node.getLocId()))
				System.err.println("This file node already visited");
			fileLocIdToListIdx.put(node.getLocId(), this.id);

			if (node.cf.getPreviousVersionsCount() != 0 && node.cf.getPreviousIndicesCount() != 0) {
				prevRevIdx = node.cf.getPreviousVersions(0);
				prevFileIdx = node.cf.getPreviousIndices(0);
				String prevFileLocId = prevRevIdx + " " + prevFileIdx;
				// check if the previous file already in a list
				if (fileLocIdToListIdx.containsKey(prevFileLocId)) {
					int prevListIdx = fileLocIdToListIdx.get(prevFileLocId);
					System.out.println("visited prev file now merge " + this.id + " to " + prevListIdx);
					lists.get(prevListIdx).merge(this);
				} else {
					if (!links.containsKey(prevRevIdx))
						links.put(prevRevIdx, new HashMap<Integer, List<Integer>>());
					if (!links.get(prevRevIdx).containsKey(prevFileIdx))
						links.get(prevRevIdx).put(prevFileIdx, new ArrayList<Integer>());
					links.get(prevRevIdx).get(prevFileIdx).add(this.id);
				}
			} else {
				prevRevIdx = -1;
				prevFileIdx = -1;
			}
		}

		public void merge(FileChangeLinkedList list) {
			System.out.println(list.id + " merge to " + this.id);
			for (String locId : list.fileLocIds) {
				this.fileLocIds.add(locId);
				// replace old list idx
				if (!fileLocIdToListIdx.containsKey(locId))
					System.err.println(" locId not exits err ");
				fileLocIdToListIdx.put(locId, this.id);
			}
			this.revIdxToNode.putAll(list.revIdxToNode);
//			System.out.println(lists.size() + " " + list.id);
			if (list.id != lists.size())
				lists.set(list.id, null);
		}

		public int getPrevFileIdx() {
			return this.prevFileIdx;
		}

		public int getPrevRevIdx() {
			return this.prevRevIdx;
		}
	}

	public static class FileNode {
		ChangedFile cf = null;
		int revIdx = -1;
		int fileIdx = -1;
		String locId = null;

		public FileNode(ChangedFile cf, int revIdx, int fileIdx) {
			if (cf == null)
				System.err.println("err null ChangedFile");
			this.cf = cf;
			this.revIdx = revIdx;
			this.fileIdx = fileIdx;
		}

		public String getLocId() {
			if (locId == null)
				locId = revIdx + " " + fileIdx;
			return locId;
		}
	}
}
