package boa.functions.code.change;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

public class FileTree {

	private final FileChangeForest forest;
	private TreeObjectId id;
	private TreeSet<FileLocation> fileLocs = new TreeSet<FileLocation>();
	private Queue<Integer> prevRevIdxs = new LinkedList<Integer>();
	private Queue<Integer> prevFileIdxs = new LinkedList<Integer>();
	// refactoring info
	public HashSet<FileLocation> fileBeforeRef = new HashSet<FileLocation>();
	public HashSet<FileLocation> fileAfterRef = new HashSet<FileLocation>();

	public FileTree(FileChangeForest fileChangeForest, FileNode node, int listIdx) {
		this.forest = fileChangeForest;
		this.id = new TreeObjectId(listIdx);
		add(node);
	}

	public boolean linkAll() {
		while (!prevRevIdxs.isEmpty()) {
			int prevRevIdx = prevRevIdxs.poll();
			int prevFileIdx = prevFileIdxs.poll();
			RevNode prevRev = forest.gd.revIdxMap.get(prevRevIdx);
			if (!add(new FileNode(prevRev.getRevision().getFiles(prevFileIdx), prevRev, prevFileIdx)))
				return false;
		}
		return true;
	}

	private boolean add(FileNode node) {
		if (this.forest.debug)
			System.out.println("try to add node " + node.getLoc() + " " + node.getChangedFile().getChange()
					+ " to list " + this.id);
		// check if the node is added by some lists
		if (forest.gd.fileLocIdToNode.containsKey(node.getLoc())) {
			int listIdx = forest.gd.fileLocIdToNode.get(node.getLoc()).getTreeObjectId().getAsInt();
			if (listIdx != this.id.getAsInt()) {
				if (this.forest.debug)
					System.out.println("node " + node.getLoc() + " already added to list " + listIdx);
				forest.trees.get(listIdx).merge(this);
				linkAll();
				if (this.forest.debug)
					System.out.println("drop list " + this.id);
				return false;
			}
			return true;
		}
		// update tree
		fileLocs.add(node.getLoc());
		// node update tree id
		node.setTreeObjectId(this.id);
		// update global nodes
		forest.gd.fileLocIdToNode.put(node.getLoc(), node);
		String oid = node.getChangedFile().getObjectId();
		if (!forest.gd.fileObjectIdToLocs.containsKey(oid))
			forest.gd.fileObjectIdToLocs.put(oid, new TreeSet<FileLocation>());
		forest.gd.fileObjectIdToLocs.get(oid).add(node.getLoc());
		// update prev queues
		if (node.getChangedFile().getPreviousVersionsCount() != 0
				&& node.getChangedFile().getPreviousIndicesCount() != 0) {
			prevRevIdxs.addAll(node.getChangedFile().getPreviousVersionsList());
			prevFileIdxs.addAll(node.getChangedFile().getPreviousIndicesList());
		}
		return true;
	}

	public void merge(FileTree tree) {
		if (this.forest.debug)
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
}