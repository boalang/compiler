package boa.functions.refactoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Queue;

import boa.functions.refactoring.BoaRefactoringPredictionIntrinsics.Rev;
import boa.types.Diff.ChangedFile;

public class FileChangeLinkedLists {

	private HashMap<Integer, Rev> revIdxMap = null;
	private List<FileChangeLinkedList> lists = new ArrayList<FileChangeLinkedList>();
	private HashMap<String, Integer> fileLocIdToListIdx = new HashMap<String, Integer>();
	private boolean debug = false;

	public FileChangeLinkedLists(HashMap<Integer, Rev> revIdxMap, boolean debug) {
		this.revIdxMap = revIdxMap;
		this.debug = debug;
		updateLists();
	}

	private void updateLists() {
		for (int i = revIdxMap.size() - 1; i >= 0; i--) {
			Rev r = revIdxMap.get(i);
			for (FileNode fn : r.getJavaFileNodes()) {
				if (!fileLocIdToListIdx.containsKey(fn.getLocId())) {
					FileChangeLinkedList list = new FileChangeLinkedList(fn, lists.size());
					if (list.linkAll())
						lists.add(list);
				}
			}
		}
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
		public Queue<Integer> prevRevIdxs = new LinkedList<Integer>();
		public Queue<Integer> prevFileIdxs = new LinkedList<Integer>();
		// refactoring
		public TreeSet<Integer> refRevIdxs = new TreeSet<Integer>();

		public FileChangeLinkedList(FileNode node, int listIdx) {
			this.id = listIdx;
			add(node);
		}

		public boolean linkAll() {
			while (!prevRevIdxs.isEmpty()) {
				int prevRevIdx = prevRevIdxs.poll();
				int prevFileIdx = prevFileIdxs.poll();
				Rev prevRev = revIdxMap.get(prevRevIdx);
				if (!add(new FileNode(prevRev.rev.getFiles(prevFileIdx), prevRev, prevFileIdx)))
					return false;
			}
			return true;
		}

		private boolean add(FileNode node) {
			if (debug)
				System.out.println("try to add node " 
						+ node.getLocId() + " " + node.cf.getChange() + " to list " + this.id);
			// check if the node is added by some lists
			if (fileLocIdToListIdx.containsKey(node.getLocId())) {
				int listIdx = fileLocIdToListIdx.get(node.getLocId());
				if (listIdx != this.id) {
					if (debug)
						System.out.println("node " + node.getLocId() + " already added to list " + listIdx);
					lists.get(listIdx).merge(this);
					if (debug)
						System.out.println("drop list " + this.id);
					return false;
				}
				return true;
			}
			// update list
			fileLocIds.add(node.getLocId());
			revIdxToNode.put(node.revIdx, node);
			fileLocIdToListIdx.put(node.getLocId(), this.id);
			if (node.cf.getPreviousVersionsCount() != 0 && node.cf.getPreviousIndicesCount() != 0) {
				prevRevIdxs.addAll(node.cf.getPreviousVersionsList());
				prevFileIdxs.addAll(node.cf.getPreviousIndicesList());
			}
			return true;
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
			if (debug)
				System.out.println("list " + this.id + " merge list " + list.id);
			for (String locId : list.fileLocIds) {
				this.fileLocIds.add(locId);
				fileLocIdToListIdx.put(locId, this.id);
			}
			this.revIdxToNode.putAll(list.revIdxToNode);
			// merge queues
			while (!list.prevRevIdxs.isEmpty()) {
				int prevRevIdx = list.prevRevIdxs.poll();
				int prevFileIdx = list.prevFileIdxs.poll();
				if (!fileLocIds.contains(prevRevIdx + " " + prevFileIdx)) {
					this.prevRevIdxs.offer(prevRevIdx);
					this.prevFileIdxs.offer(prevFileIdx);
				}
			}
			this.refRevIdxs.addAll(list.refRevIdxs);
			if (list.id < lists.size())
				lists.set(list.id, null);
			linkAll();
		}
	}

}
