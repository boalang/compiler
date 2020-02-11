package boa.functions.refactoring;

import static boa.functions.refactoring.BoaRefactoringIntrinsics.isJavaFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import boa.functions.refactoring.BoaRefactoringPredictionIntrinsics.Rev;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

public class FileChangeLinkedLists {

	private HashMap<Integer, Rev> revIdxMap = null;
	private List<FileChangeLinkedList> lists = new ArrayList<FileChangeLinkedList>();
	private HashMap<String, Integer> fileLocIdToListIdx = new HashMap<String, Integer>();
	// rev idx to file idx to list idx
	private HashMap<Integer, HashMap<Integer, List<Integer>>> links = new HashMap<Integer, HashMap<Integer, List<Integer>>>();

	public FileChangeLinkedLists(HashMap<Integer, Rev> revIdxMap) {
		this.revIdxMap = revIdxMap;
		updateLists();
	}

	public void updateLists() {
		for (int i = revIdxMap.size() - 1; i >= 0; i--) {
			Rev r = revIdxMap.get(i);
			updateFileChangeLinkedLists(r);
		}
	}

	public void updateFileChangeLinkedLists(Rev r) {
		// linking requests
		HashMap<Integer, ChangedFile> files = getFilesFrom(r.rev);
		if (links.containsKey(r.revIdx)) {
			for (Entry<Integer, List<Integer>> entry : links.remove(r.revIdx).entrySet()) {
				int fileIdx = entry.getKey();
				List<Integer> listIdxs = entry.getValue();
				ChangedFile cf = files.remove(fileIdx);

				FileChangeLinkedList list = null;
				if (listIdxs.size() > 1) {
					System.out.println("linking request merge lists with size " + listIdxs.size());
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
					FileNode node = new FileNode(cf, r, fileIdx);
					if (list.getPrevFileIdx() != fileIdx && list.getPrevRevIdx() != r.revIdx)
						System.err.println("err 2");
					System.out.println("linking request add ");
					list.add(node);
				}
			}
		}
		// update files
		for (Entry<Integer, ChangedFile> entry : files.entrySet()) {
			ChangedFile cf = entry.getValue();
			if (isJavaFile(cf.getName())) {
				FileNode node = new FileNode(cf, r, entry.getKey());
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
	
	public HashMap<Integer, HashMap<Integer, List<Integer>>> getLinks() {
		return links;
	}

	public List<FileChangeLinkedList> getLists() {
		return lists;
	}
	
	public HashMap<String, Integer> getFileLocIdToListIdxMap() {
		return fileLocIdToListIdx;
	}

	public class FileChangeLinkedList {
		public int id;
		public HashSet<String> fileLocIds = new HashSet<String>();
		public TreeMap<Integer, FileNode> revIdxToNode = new TreeMap<Integer, FileNode>();
		public int prevRevIdx = -1;
		public int prevFileIdx = -1;
		// refactoring
		public TreeSet<Integer> refRevIdxs = new TreeSet<Integer>();

		public FileChangeLinkedList(FileNode node, int listIdx) {
			this.id = listIdx;
			add(node);
		}

		public void add(FileNode node) {
			System.out.println("try to add node " + node.getLocId() + " " + node.cf.getChange() + " to list " + this.id);
			// check if the node is added by some lists
			if (fileLocIdToListIdx.containsKey(node.getLocId())) {
				int listIdx = fileLocIdToListIdx.get(node.getLocId());
				System.out.println("node " + node.getLocId() + " already added to list " + listIdx);
				lists.get(listIdx).merge(this);
				return;
			}

			fileLocIds.add(node.getLocId());
			revIdxToNode.put(node.revIdx, node);
			fileLocIdToListIdx.put(node.getLocId(), this.id);

			if (node.cf.getPreviousVersionsCount() != 0 && node.cf.getPreviousIndicesCount() != 0) {
				// update linking requests
				prevRevIdx = node.cf.getPreviousVersions(0);
				prevFileIdx = node.cf.getPreviousIndices(0);
				if (!links.containsKey(prevRevIdx))
					links.put(prevRevIdx, new HashMap<Integer, List<Integer>>());
				if (!links.get(prevRevIdx).containsKey(prevFileIdx))
					links.get(prevRevIdx).put(prevFileIdx, new ArrayList<Integer>());
				links.get(prevRevIdx).get(prevFileIdx).add(this.id);
				System.out.println("list " + this.id + " add linking request to " + prevRevIdx + " " + prevFileIdx);
			} else if (node.cf.getChange() == ChangeKind.ADDED && node.rev.getParentsCount() > 1) {
				// link ADDED files in merge commit to feature branch
				System.out.println(node.rev.getParentsCount());
				FileNode prevNode = findNode(node.cf.getName(), node.rev.getParents(1));
				if (prevNode != null) {
					System.out.println("found");
					this.add(prevNode);
				}
			} else {
				prevRevIdx = -1;
				prevFileIdx = -1;
			}
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

		public void merge(FileChangeLinkedList list) {
			System.out.println("merge list " + list.id + " to " + this.id);
			for (String locId : list.fileLocIds) {
				this.fileLocIds.add(locId);
				// replace old list idx
				if (!fileLocIdToListIdx.containsKey(locId))
					System.err.println(" locId not exits err ");
				fileLocIdToListIdx.put(locId, this.id);
			}
			this.revIdxToNode.putAll(list.revIdxToNode);
			if (list.id < lists.size())
				lists.set(list.id, null);
		}

		public int getPrevFileIdx() {
			return this.prevFileIdx;
		}

		public int getPrevRevIdx() {
			return this.prevRevIdx;
		}
	}

	

}
