package boa.functions.code.change.file;

import java.util.HashSet;
import java.util.Stack;
import java.util.TreeSet;

import boa.functions.code.change.RevNode;
import boa.functions.code.change.TreeObjectId;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

public class FileTree {

	private final FileChangeForest forest;
	private TreeObjectId id;
	private TreeSet<ChangedFileLocation> fileLocs = new TreeSet<ChangedFileLocation>();
	private Stack<ChangedFileLocation> prevLocs = new Stack<ChangedFileLocation>(); //TODO use node
	// refactoring info
	public HashSet<ChangedFileLocation> fileBeforeRef = new HashSet<ChangedFileLocation>();
	public HashSet<ChangedFileLocation> fileAfterRef = new HashSet<ChangedFileLocation>();

	public FileTree(FileChangeForest fileChangeForest, ChangedFileNode node, int treeIdx) {
		this.forest = fileChangeForest;
		this.id = new TreeObjectId(treeIdx);
		add(node);
	}

	public boolean linkAll() {
		while (!prevLocs.isEmpty()) {
			ChangedFileLocation loc = prevLocs.pop();
			if (this.forest.debug)
				System.out.println("pop parent " + loc);
			RevNode prevRev = forest.db.revIdxMap.get(loc.getRevIdx());
			if (!add(new ChangedFileNode(prevRev.getRevision().getFiles(loc.getIdx()), prevRev, loc)))
				return false;
		}
		return true;
	}

	private boolean add(ChangedFileNode node) {
		if (forest.debug)
			System.out.println("try to add node " + node.getLoc() + " " + node.getChangedFile().getChange()
					+ " to list " + this.id);
		// case 1: check if the node is added by some trees
		if (forest.db.fileDB.containsKey(node.getLoc())) {
			int listIdx = forest.db.fileDB.get(node.getLoc()).getTreeId().getAsInt();
			if (listIdx != this.id.getAsInt()) {
				if (forest.debug)
					System.out.println("node " + node.getLoc() + " already added to list " + listIdx);
				String thisId = id.toString();
				forest.getTreesAsList().get(listIdx).merge(this).linkAll();
				if (this.forest.debug)
					System.err.println("drop list " + thisId);
				return false;
			}
			if (forest.debug)
				System.out.println("node " + node.getLoc() + " already in the same tree");
			return true;
		}

		// case 2: update tree
		fileLocs.add(node.getLoc());
		// node update tree id
		node.setTreeObjectId(this.id);
		// update global nodes
		forest.db.fileDB.put(node.getLoc(), node);
		forest.db.fileNames.add(node.getChangedFile().getName());
		// update prev queues
		updatePrevLocs(node);
		// push 2nd parent first for dfs first-parent branch first
		if (node.hasSecondParentLoc()) {
			prevLocs.push(node.getSecondParentLoc());
			if (forest.debug)
				System.out.println("push 2nd parent " + node.getSecondParentLoc());
		}
		if (node.hasFirstParentLoc()) {
			prevLocs.push(node.getFirstParentLoc());
			if (forest.debug)
				System.out.println("push 1st parent " + node.getFirstParentLoc());
		}
		return true;
	}

	private void updatePrevLocs(ChangedFileNode node) {
		Revision r = node.getRev().getRevision();
		int prevCount = node.getChangedFile().getPreviousVersionsCount();
		if (forest.debug)
			System.out.println("node " + node.getLoc() + " has revision parents: " + r.getParentsCount()
					+ " and conetent parents: " + prevCount);
		if (r.getParentsCount() == 1 && prevCount > 1) {
			ChangedFileLocation prevLoc = findPrevious(node.getChangedFile(), r.getParents(0));
			if (prevLoc != null)
				node.setFirstParentLoc(prevLoc);
		} else if (r.getParentsCount() == 1 && prevCount == 1) {
			int revIdx = node.getChangedFile().getPreviousVersions(0);
			int fileIdx = node.getChangedFile().getPreviousIndices(0);
			node.setFirstParentLoc(new ChangedFileLocation(revIdx, fileIdx));
		} else if (r.getParentsCount() == 2) {
			for (int i = 0; i < r.getParentsCount(); i++) {
				ChangedFileLocation prevLoc = findPrevious(node.getChangedFile(), r.getParents(i));
				if (prevLoc != null)
					if (i == 0)
						node.setFirstParentLoc(prevLoc);
					else
						node.setSecondParentLoc(prevLoc);
			}
		}
	}

	// find previous file from parent r
	private ChangedFileLocation findPrevious(ChangedFile cf, int revParentIdx) {
		String prevName = cf.getChange() == ChangeKind.RENAMED ? cf.getPreviousNames(0) : cf.getName();
		RevNode cur = forest.db.revIdxMap.get(revParentIdx);
		do {
			ChangedFileLocation loc = getFileLocationFrom(prevName, cur);
			if (loc != null)
				return loc;
			if (cur.getRevision().getParentsCount() == 0)
				return null;
			// check first-parent branch
			cur = forest.db.revIdxMap.get(cur.getRevision().getParents(0));
		} while (true);
	}

	private ChangedFileLocation getFileLocationFrom(String filePath, RevNode r) {
		for (ChangedFile cf : r.getRevision().getFilesList()) {
			if (cf.getName().equals(filePath))
				return new ChangedFileLocation(cf.getRevisionIdx(), cf.getFileIdx());
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
		while (!tree.prevLocs.isEmpty()) {
			ChangedFileLocation loc = tree.prevLocs.pop();
			if (!fileLocs.contains(loc)) {
				this.prevLocs.push(loc);
			}
		}
		return this;
	}

	public TreeObjectId getId() {
		return id;
	}

	public TreeSet<ChangedFileLocation> getFileLocs() {
		return fileLocs;
	}
}