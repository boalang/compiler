package boa.functions.code.change;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

public class FileTree {

	private final FileChangeForest forest;
	private TreeObjectId id;
	private TreeSet<FileLocation> fileLocs = new TreeSet<FileLocation>();
	private Queue<FileLocation> prevFileLocs = new LinkedList<FileLocation>();
	// refactoring info
	public HashSet<FileLocation> fileBeforeRef = new HashSet<FileLocation>();
	public HashSet<FileLocation> fileAfterRef = new HashSet<FileLocation>();

	public FileTree(FileChangeForest fileChangeForest, FileNode node, int treeIdx) {
		this.forest = fileChangeForest;
		this.id = new TreeObjectId(treeIdx);
		add(node);
	}

	public boolean linkAll() {
		while (!prevFileLocs.isEmpty()) {
			FileLocation loc = prevFileLocs.poll();
			RevNode prevRev = forest.gd.revIdxMap.get(loc.getRevIdx());
			if (!add(new FileNode(prevRev.getRevision().getFiles(loc.getIdx()), prevRev, loc)))
				return false;
		}
		return true;
	}

	private boolean add(FileNode node) {
		if (this.forest.debug)
			System.out.println("try to add node " + node.getLoc() + " " + node.getChangedFile().getChange()
					+ " to list " + this.id);
		// check if the node is added by some trees
		if (forest.gd.fileLocIdToNode.containsKey(node.getLoc())) {
			int listIdx = forest.gd.fileLocIdToNode.get(node.getLoc()).getTreeObjectId().getAsInt();
			if (listIdx != this.id.getAsInt()) {
				if (this.forest.debug)
					System.out.println("node " + node.getLoc() + " already added to list " + listIdx);
				forest.trees.get(listIdx).merge(this).linkAll();
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
			for (int i = 0; i < node.getChangedFile().getPreviousVersionsCount(); i++) {
				int revIdx = node.getChangedFile().getPreviousVersions(i);
				int fileIdx = node.getChangedFile().getPreviousIndices(i);
				FileLocation loc = new FileLocation(revIdx, fileIdx);
				node.getPrevLocs().add(loc);
				prevFileLocs.offer(loc);
			}
		}
		return true;
	}

	public FileTree merge(FileTree tree) {
		if (this.forest.debug)
			System.out.println("list " + this.id + " merge list " + tree.id);
		// add nodes
		this.fileLocs.addAll(tree.fileLocs);
		// update list id
		tree.id.setId(this.id.getAsInt());
		// merge queues
		while (!tree.prevFileLocs.isEmpty()) {
			FileLocation loc = tree.prevFileLocs.poll();
			if (!fileLocs.contains(loc)) {
				this.prevFileLocs.offer(loc);
			}
		}
		return this;
	}

	public TreeObjectId getId() {
		return id;
	}

	public TreeSet<FileLocation> getFileLocs() {
		return fileLocs;
	}
}