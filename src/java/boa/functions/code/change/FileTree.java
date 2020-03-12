package boa.functions.code.change;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

public class FileTree {

	private final FileChangeForest trees;
	private TreeObjectId id;
	private TreeSet<FileLocation> fileLocs = new TreeSet<FileLocation>();
	private Queue<Integer> prevRevIdxs = new LinkedList<Integer>();
	private Queue<Integer> prevFileIdxs = new LinkedList<Integer>();
	// refactoring info
	public HashSet<FileLocation> fileBeforeRef = new HashSet<FileLocation>();
	public HashSet<FileLocation> fileAfterRef = new HashSet<FileLocation>();

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
		node.setTreeObjectId(this.id);
		// update global nodes
		trees.fileLocIdToNode.put(node.getLocId(), node);
		String oid = node.getChangedFile().getObjectId();
		if (!trees.fileObjectIdToLocs.containsKey(oid))
			trees.fileObjectIdToLocs.put(oid, new TreeSet<FileLocation>());
		trees.fileObjectIdToLocs.get(oid).add(node.getLocId());
		// update prev queues
		if (node.getChangedFile().getPreviousVersionsCount() != 0
				&& node.getChangedFile().getPreviousIndicesCount() != 0) {
			prevRevIdxs.addAll(node.getChangedFile().getPreviousVersionsList());
			prevFileIdxs.addAll(node.getChangedFile().getPreviousIndicesList());
		}
		return true;
	}

	public void merge(FileTree tree) {
		if (this.trees.debug)
			System.out.println("list " + this.id + " merge list " + tree.id);
		// add nodes
		this.fileLocs.addAll(tree.fileLocs);
		// update list id
		tree.id.setId(this.id.getAsInt());
		// merge queues
		while (!tree.prevRevIdxs.isEmpty()) {
			int prevRevIdx = tree.prevRevIdxs.poll();
			int prevFileIdx = tree.prevFileIdxs.poll();
			if (!fileLocs.contains(new FileLocation(prevRevIdx, prevFileIdx))) {
				this.prevRevIdxs.offer(prevRevIdx);
				this.prevFileIdxs.offer(prevFileIdx);
			}
		}
	}

	public TreeObjectId getId() {
		return id;
	}
	
	public TreeSet<FileLocation> getFileLocs() {
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