package boa.functions.code.change;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class FileTree {

	private final FileChangeForest trees;
	private TreeObjectId id;
	private HashSet<String> fileLocs = new HashSet<String>();
	private Queue<Integer> prevRevIdxs = new LinkedList<Integer>();
	private Queue<Integer> prevFileIdxs = new LinkedList<Integer>();
	// refactoring info
	public HashSet<String> refLocs = new HashSet<String>();

	public FileTree(FileChangeForest trees, FileNode node, int listIdx) {
		this.trees = trees;
		this.id = new TreeObjectId(listIdx);
		add(node);
	}

	public boolean linkAll() {
		while (!prevRevIdxs.isEmpty()) {
			int prevRevIdx = prevRevIdxs.poll();
			int prevFileIdx = prevFileIdxs.poll();
			RevNode prevRev = trees.revIdxMap.get(prevRevIdx);
			if (!add(new FileNode(prevRev.getRevision().getFiles(prevFileIdx), prevRev, prevFileIdx)))
				return false;
		}
		return true;
	}

	private boolean add(FileNode node) {
		if (this.trees.debug)
			System.out.println("try to add node " + node.getLocId() + " " + node.getChangedFile().getChange()
					+ " to list " + this.id);
		// check if the node is added by some lists
		if (trees.fileLocIdToNode.containsKey(node.getLocId())) {
			int listIdx = trees.fileLocIdToNode.get(node.getLocId()).getTreeObjectId().getAsInt();
			if (listIdx != this.id.getAsInt()) {
				if (this.trees.debug)
					System.out.println("node " + node.getLocId() + " already added to list " + listIdx);
				trees.trees.get(listIdx).merge(this);
				linkAll();
				if (this.trees.debug)
					System.out.println("drop list " + this.id);
				return false;
			}
			return true;
		}
		// update tree
		fileLocs.add(node.getLocId());
		// node update tree id
		node.setListObjectId(this.id);
		// update global nodes
		trees.fileLocIdToNode.put(node.getLocId(), node);
		String oid = node.getChangedFile().getObjectId();
		if (!trees.fileObjectIdToLocs.containsKey(oid))
			trees.fileObjectIdToLocs.put(oid, new HashSet<String>());
		trees.fileObjectIdToLocs.get(oid).add(node.getLocId());
		// update prev queues
		if (node.getChangedFile().getPreviousVersionsCount() != 0
				&& node.getChangedFile().getPreviousIndicesCount() != 0) {
			prevRevIdxs.addAll(node.getChangedFile().getPreviousVersionsList());
			prevFileIdxs.addAll(node.getChangedFile().getPreviousIndicesList());
		}
		return true;
	}

	public void merge(FileTree list) {
		if (this.trees.debug)
			System.out.println("list " + this.id + " merge list " + list.id);
		// add nodes
		this.fileLocs.addAll(list.fileLocs);
		// update list id
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
	}

	public TreeObjectId getId() {
		return id;
	}
	
	public HashSet<String> getFileLocs() {
		return fileLocs;
	}

	public class TreeObjectId {
		public int id = -1;

		public TreeObjectId(int id) {
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