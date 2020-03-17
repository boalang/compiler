package boa.functions.code.change;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeSet;

import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

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
//			System.out.println("merge");
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
		forest.gd.fileNames.add(node.getChangedFile().getName());
		String oid = node.getChangedFile().getObjectId();
		if (!forest.gd.fileObjectIdToLocs.containsKey(oid))
			forest.gd.fileObjectIdToLocs.put(oid, new TreeSet<FileLocation>());
		forest.gd.fileObjectIdToLocs.get(oid).add(node.getLoc());
		// update prev queues
		for (FileLocation loc : getPrevLocs(node)) {
			node.getPrevLocs().add(loc);
			prevFileLocs.offer(loc);
		}
		return true;
	}
	
	private List<FileLocation> getPrevLocs(FileNode node) {
		List<FileLocation> res = new ArrayList<FileLocation>();
		Revision r = node.getRev().getRevision();
		int prevCount = node.getChangedFile().getPreviousVersionsCount();
		if ((r.getParentsCount() == 1 && prevCount > 1) 
				|| (r.getParentsCount() == 2)) {
			for (int i = 0; i < r.getParentsCount(); i++) {
				FileLocation prevLoc = findPrevious(node.getChangedFile(), r.getParents(i));
				if (prevLoc != null)
					res.add(prevLoc);
			}
		} else if (r.getParentsCount() == 1 && prevCount == 1) {
			int revIdx = node.getChangedFile().getPreviousVersions(0);
			int fileIdx = node.getChangedFile().getPreviousIndices(0);
			FileLocation prevLoc =  new FileLocation(revIdx, fileIdx);
			res.add(prevLoc);
		}
		return res;
	}

	// find previous file from parent r
	private FileLocation findPrevious(ChangedFile cf, int revParentIdx) {
		String prevName = cf.getChange() == ChangeKind.RENAMED ? cf.getPreviousNames(0) : cf.getName();
		RevNode cur = forest.gd.revIdxMap.get(revParentIdx);
		do {
			FileLocation loc = getFileLocationFrom(prevName, cur);
			if (loc != null)
				return loc;
			if (cur.getRevision().getParentsCount() == 0)
				return null;
			// first parent in main branch
			cur = forest.gd.revIdxMap.get(cur.getRevision().getParents(0));
		} while (true);
	}

	private FileLocation getFileLocationFrom(String filePath, RevNode r) {
		for (ChangedFile cf : r.getRevision().getFilesList()) {
			if (cf.getName().equals(filePath))
				return new FileLocation(cf.getRevisionIdx(), cf.getFileIdx());
		}
		return null;
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