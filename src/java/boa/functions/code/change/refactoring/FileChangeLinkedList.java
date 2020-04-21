package boa.functions.code.change.refactoring;

import static boa.functions.code.change.refactoring.FileChangeLinkedLists.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class FileChangeLinkedList {

	private final FileChangeLinkedLists fileChangeLinkedLists;
	private ListObjectId id;
	private HashSet<String> fileLocs = new HashSet<String>();
	private Queue<Integer> prevRevIdxs = new LinkedList<Integer>();
	private Queue<Integer> prevFileIdxs = new LinkedList<Integer>();
	// refactoring info
	public HashSet<String> refLocs = new HashSet<String>();

	public FileChangeLinkedList(FileChangeLinkedLists fileChangeLinkedLists, FileNode node, int listIdx) {
		this.fileChangeLinkedLists = fileChangeLinkedLists;
		this.id = new ListObjectId(listIdx);
		add(node);
	}

	public boolean linkAll() {
		while (!prevRevIdxs.isEmpty()) {
			int prevRevIdx = prevRevIdxs.poll();
			int prevFileIdx = prevFileIdxs.poll();
			Rev prevRev = revIdxMap.get(prevRevIdx);
			if (!add(new FileNode(prevRev.getRevision().getFiles(prevFileIdx), prevRev, prevFileIdx)))
				return false;
		}
		return true;
	}

	private boolean add(FileNode node) {
		if (this.fileChangeLinkedLists.debug)
			System.out.println("try to add node " + node.getLocId() + " " + node.getChangedFile().getChange()
					+ " to list " + this.id);
		// check if the node is added by some lists
		if (fileLocIdToNode.containsKey(node.getLocId())) {
			int listIdx = fileLocIdToNode.get(node.getLocId()).getListObjectId().getAsInt();
			if (listIdx != this.id.getAsInt()) {
				if (this.fileChangeLinkedLists.debug)
					System.out.println("node " + node.getLocId() + " already added to list " + listIdx);
				lists.get(listIdx).merge(this);
				if (this.fileChangeLinkedLists.debug)
					System.out.println("drop list " + this.id);
				return false;
			}
			return true;
		}
		// update list and global maps
		fileLocs.add(node.getLocId());
		node.setListObjectId(this.id);
		fileLocIdToNode.put(node.getLocId(), node);
		String oid = node.getChangedFile().getObjectId();
		if (!fileObjectIdToLocs.containsKey(oid))
			fileObjectIdToLocs.put(oid, new HashSet<String>());
		fileObjectIdToLocs.get(oid).add(node.getLocId());
		// update prev queues
		if (node.getChangedFile().getPreviousVersionsCount() != 0
				&& node.getChangedFile().getPreviousIndicesCount() != 0) {
			prevRevIdxs.addAll(node.getChangedFile().getPreviousVersionsList());
			prevFileIdxs.addAll(node.getChangedFile().getPreviousIndicesList());
		}
		return true;
	}

	public void merge(FileChangeLinkedList list) {
		if (this.fileChangeLinkedLists.debug)
			System.out.println("list " + this.id + " merge list " + list.id);
		// add nodes and update their list id
		this.fileLocs.addAll(list.fileLocs);
		list.id.setId(this.id.getAsInt());
		// merge queues
		while (!list.prevRevIdxs.isEmpty()) {
			int prevRevIdx = list.prevRevIdxs.poll();
			int prevFileIdx = list.prevFileIdxs.poll();
			if (!fileLocs.contains(prevRevIdx + " " + prevFileIdx)) {
				this.prevRevIdxs.offer(prevRevIdx);
				this.prevFileIdxs.offer(prevFileIdx);
			}
		}
		this.refLocs.addAll(list.refLocs);
		linkAll();
	}

	public ListObjectId getId() {
		return id;
	}
	
	public HashSet<String> getFileLocs() {
		return fileLocs;
	}

	public class ListObjectId {
		public int id = -1;

		public ListObjectId(int id) {
			this.id = id;
		}

		public int getAsInt() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}
	}
}