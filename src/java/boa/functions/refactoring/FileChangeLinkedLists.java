package boa.functions.refactoring;

import static boa.functions.BoaAstIntrinsics.getCodeChange;
import static boa.functions.BoaIntrinsics.getRevision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import boa.types.Code.CodeRefactoring;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import boa.types.Toplevel.Project;

import static boa.functions.refactoring.BoaRefactoringPredictionIntrinsics.*;

public class FileChangeLinkedLists {
	
	protected static HashMap<Integer, Rev> revIdxMap = new HashMap<Integer, Rev>();
	protected static HashMap<String, Rev> revIdMap = new HashMap<String, Rev>();
	protected static List<FileChangeLinkedList> lists = new ArrayList<FileChangeLinkedList>();
	protected static HashMap<String, HashSet<String>> fileObjectIdToLocs = new HashMap<String, HashSet<String>>();
	protected static HashMap<String, FileNode> fileLocIdToNode = new HashMap<String, FileNode>();
	
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
	private HashSet<Integer> observedRevIdx = new HashSet<Integer>();

	public void updateRefLists(Project p, HashSet<String> refRevIds, Set<String> refTypes) {
		for (String id : refRevIds) {
			Rev r = revIdMap.get(id);
			List<CodeRefactoring> refs = refTypes == null ? getCodeChange(p, r.getRevision()).getRefactoringsList()
					: getRefactorings(p, r.getRevision(), refTypes);
			for (CodeRefactoring ref : refs) {
				String beforeFilePath = ref.getLeftSideLocations(0).getFilePath();
				FileNode fn = findNode(beforeFilePath, r.getRevision().getParents(0));
				if (!fileLocIdToNode.containsKey(fn.getLocId()))
					System.err.println("err 1");
				int ListIdx = fileLocIdToNode.get(fn.getLocId()).getListObjectId().getAsInt();
				FileChangeLinkedList list = lists.get(ListIdx);
				if (!fileLocIdToNode.get(fn.getLocId()).equals(fn)) {
					System.err.println(ref.getDescription());
				}
				list.refLocs.add(fn.getLocId());
			}
		}

		for (FileChangeLinkedList list : lists)
			if (list.refLocs.size() > 0)
				refListIdxs.add(list.getId().getAsInt());
			else
				noRefListIdxs.add(list.getId().getAsInt());

		refNodeLocs = getRefNodeIdxs();
		noRefNodeLocs = getNoRefNodeIdxs();
	}

	private HashSet<String> getNoRefNodeIdxs() {
		HashSet<String> noRefNodeIdxs = new HashSet<String>();
		for (int ListIdx : noRefListIdxs) {
			FileChangeLinkedList list = lists.get(ListIdx);
			for (String locId : list.getFileLocs()) {
				FileNode fn = fileLocIdToNode.get(locId);
				// no deleted files
				if (fn.getChangedFile().getChange() != ChangeKind.DELETED) {
					noRefNodeIdxs.add(fn.getLocId());
//					observedRevIdx.add(fn.getRevIdx());
				}
			}
		}
		return noRefNodeIdxs;
	}

	private HashSet<String> getRefNodeIdxs() {
		HashSet<String> refNodeIdxs = new HashSet<String>();
		for (int ListIdx : refListIdxs) {
			FileChangeLinkedList list = lists.get(ListIdx);
			for (String locId : list.refLocs) {
				FileNode fn = fileLocIdToNode.get(locId);
				if (fn.getChangedFile().getChange() == ChangeKind.DELETED)
					System.err.println("**** ref cf is DELETED");
				refNodeIdxs.add(fn.getLocId());
//				observedRevIdx.add(fn.getRevIdx());
			}
		}
		return refNodeIdxs;
	}

	public FileNode findNode(String fileName, int parentIdx) {
		Rev cur = revIdxMap.get(parentIdx);
		while (true) {
			for (int i = 0; i < cur.getRevision().getFilesCount(); i++) {
				ChangedFile cf = cur.getRevision().getFiles(i);
				observedRevIdx.add(cur.getRevIdx());
				if (cf.getName().equals(fileName)) {
					// other files with the same file content(object id)
					if (fileObjectIdToLocs.containsKey(cf.getObjectId()) 
							&& fileObjectIdToLocs.get(cf.getObjectId()).size() > 1) {
						String fileLoc = cf.getRevisionIdx() + " " + cf.getFileIdx();						
						for (String locId : fileObjectIdToLocs.get(cf.getObjectId())) {
							if (!locId.equals(fileLoc)) {
								observedRevIdx.add(Integer.parseInt(locId.split(" ")[0]));
							}
						}
					}
					return new FileNode(cf, cur, i);
				}
			}
			if (cur.getRevision().getParentsCount() == 0)
				return null;
			cur = revIdxMap.get(cur.getRevision().getParents(0));
		}
	}

	private void updateLists() {
		for (int i = revIdxMap.size() - 1; i >= 0; i--) {
			Rev r = revIdxMap.get(i);
			for (FileNode fn : r.getJavaFileNodes()) {
				if (!fileLocIdToNode.containsKey(fn.getLocId())) {
					FileChangeLinkedList list = new FileChangeLinkedList(this, fn, lists.size());
					if (list.linkAll())
						lists.add(list);
				}
			}
		}
	}
	
	// revision info
	private static HashSet<String> nContributor = new HashSet<String>();
	
	public static Rev getRev(CodeRepository cr, int idx) {
		if (revIdxMap.containsKey(idx))
			return revIdxMap.get(idx);
		Revision r = getRevision(cr, idx);
		nContributor.add(r.getAuthor().getUsername());
		Rev rev = new Rev(idx, r, nContributor.size());
		revIdxMap.put(idx, rev);
		revIdMap.put(r.getId(), rev);
		return revIdxMap.get(idx);
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
	
	public HashSet<String> getRefNodeLocs() {
		return refNodeLocs;
	}

	public HashSet<String> getNoRefNodeLocs() {
		return noRefNodeLocs;
	}

	public HashSet<Integer> getObservedRevIdx() {
		return observedRevIdx;
	}

}
