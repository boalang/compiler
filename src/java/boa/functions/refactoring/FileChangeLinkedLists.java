package boa.functions.refactoring;

import static boa.functions.BoaAstIntrinsics.getCodeChange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import boa.types.Code.CodeRefactoring;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import boa.types.Toplevel.Project;

import static boa.functions.refactoring.BoaRefactoringPredictionIntrinsics.*;

public class FileChangeLinkedLists {
	// file change linked list
	List<FileChangeLinkedList> lists = new ArrayList<FileChangeLinkedList>();
	HashMap<String, Integer> fileLocIdToListIdx = new HashMap<String, Integer>();
	boolean debug = false;

	public FileChangeLinkedLists(boolean debug) {
		this.debug = debug;
		updateLists();
	}

	// refactoring info
	private HashSet<Integer> refListIdxs = new HashSet<Integer>();
	private HashSet<Integer> noRefListIdxs = new HashSet<Integer>();
	private HashSet<String> refNodeLocs = null;
	private HashSet<String> noRefNodeLocs = null;
	private HashMap<Integer, List<FileNode>> revIdxToNodes = new HashMap<Integer, List<FileNode>>();

	public void updateRefLists(Project p, HashSet<String> refRevIds, Set<String> refTypes) {
		for (String id : refRevIds) {
			Rev r = revIdMap.get(id);
			List<CodeRefactoring> refs = refTypes == null ? getCodeChange(p, r.rev).getRefactoringsList()
					: getRefactorings(p, r.rev, refTypes);
			for (CodeRefactoring ref : refs) {
				String beforeFilePath = ref.getLeftSideLocations(0).getFilePath();
				FileNode fn = findNode(beforeFilePath, r.rev.getParents(0));
				if (!fileLocIdToListIdx.containsKey(fn.getLocId()))
					System.err.println("err 1");
				int ListIdx = fileLocIdToListIdx.get(fn.getLocId());
				FileChangeLinkedList list = lists.get(ListIdx);
				if (!list.fileLocIdToNode.get(fn.locId).equals(fn)) {
					System.out.println(ref.getDescription());
				}
				list.refLocs.add(fn.getLocId());
			}
		}

		for (FileChangeLinkedList list : lists)
			if (list.refLocs.size() > 0)
				refListIdxs.add(list.id);
			else
				noRefListIdxs.add(list.id);

		refNodeLocs = getRefNodeIdxs();
		noRefNodeLocs = getNoRefNodeIdxs();
	}

	private HashSet<String> getNoRefNodeIdxs() {
		HashSet<String> noRefNodeIdxs = new HashSet<String>();
		for (int ListIdx : noRefListIdxs) {
			FileChangeLinkedList list = lists.get(ListIdx);
			for (FileNode fn : list.fileLocIdToNode.values()) {
				// no deleted files
				if (fn.cf.getChange() != ChangeKind.DELETED) {
					noRefNodeIdxs.add(fn.getLocId());
					if (!revIdxToNodes.containsKey(fn.revIdx))
						revIdxToNodes.put(fn.revIdx, new ArrayList<FileNode>());
					revIdxToNodes.get(fn.revIdx).add(fn);
				}
			}
		}
		return noRefNodeIdxs;
	}

	private HashSet<String> getRefNodeIdxs() {
		HashSet<String> refNodeIdxs = new HashSet<String>();
		for (int ListIdx : refListIdxs) {
			FileChangeLinkedList list = lists.get(ListIdx);
			for (String Loc : list.refLocs) {
				FileNode fn = list.fileLocIdToNode.get(Loc);
				refNodeIdxs.add(fn.getLocId());
				if (!revIdxToNodes.containsKey(fn.revIdx))
					revIdxToNodes.put(fn.revIdx, new ArrayList<FileNode>());
				revIdxToNodes.get(fn.revIdx).add(fn);
			}
		}
		return refNodeIdxs;
	}

	public FileNode findNode(String fileName, int parentIdx) {
		Rev cur = revIdxMap.get(parentIdx);
		while (true) {
			for (int i = 0; i < cur.rev.getFilesCount(); i++) {
				ChangedFile cf = cur.rev.getFiles(i);
				if (cf.getName().equals(fileName))
					return new FileNode(cf, cur, i);
			}
			if (cur.rev.getParentsCount() == 0)
				return null;
			cur = revIdxMap.get(cur.rev.getParents(0));
		}
	}

	private void updateLists() {
		for (int i = revIdxMap.size() - 1; i >= 0; i--) {
			Rev r = revIdxMap.get(i);
			for (FileNode fn : r.getJavaFileNodes()) {
				if (!fileLocIdToListIdx.containsKey(fn.getLocId())) {
					FileChangeLinkedList list = new FileChangeLinkedList(this, fn, lists.size());
					if (list.linkAll())
						lists.add(list);
				}
			}
		}
	}

	public HashSet<Integer> getRefListIdxs() {
		return refListIdxs;
	}

	public HashSet<Integer> getNoRefListIdxs() {
		return noRefListIdxs;
	}

	public List<FileChangeLinkedList> getLists() {
		return lists;
	}

	public HashMap<String, Integer> getFileLocIdToListIdxMap() {
		return fileLocIdToListIdx;
	}
	
	public HashSet<String> getRefNodeLocs() {
		return refNodeLocs;
	}

	public HashSet<String> getNoRefNodeLocs() {
		return noRefNodeLocs;
	}

	public HashMap<Integer, List<FileNode>> getRevIdxToNodes() {
		return revIdxToNodes;
	}

}
