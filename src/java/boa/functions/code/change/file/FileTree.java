package boa.functions.code.change.file;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
	private Queue<ChangedFileLocation> prevFileLocs = new LinkedList<ChangedFileLocation>();
	// refactoring info
	public HashSet<ChangedFileLocation> fileBeforeRef = new HashSet<ChangedFileLocation>();
	public HashSet<ChangedFileLocation> fileAfterRef = new HashSet<ChangedFileLocation>();

	public FileTree(FileChangeForest fileChangeForest, ChangedFileNode node, int treeIdx) {
		this.forest = fileChangeForest;
		this.id = new TreeObjectId(treeIdx);
		add(node);
	}

	public boolean linkAll() {
		while (!prevFileLocs.isEmpty()) {
			ChangedFileLocation loc = prevFileLocs.poll();
			if (loc == null) // check null
				continue;
			RevNode prevRev = forest.db.revIdxMap.get(loc.getRevIdx());
			if (!add(new ChangedFileNode(prevRev.getRevision().getFiles(loc.getIdx()), prevRev, loc)))
				return false;
		}
		return true;
	}

	private boolean add(ChangedFileNode node) {
		if (this.forest.debug)
			System.out.println("try to add node " + node.getLoc() + " " + node.getChangedFile().getChange()
					+ " to list " + this.id);
		// check if the node is added by some trees
		if (forest.db.fileLocIdToNode.containsKey(node.getLoc())) {
//			System.out.println("merge");
			int listIdx = forest.db.fileLocIdToNode.get(node.getLoc()).getTreeObjectId().getAsInt();
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
		forest.db.fileLocIdToNode.put(node.getLoc(), node);
		forest.db.fileNames.add(node.getChangedFile().getName());
		// update prev queues
		updatePrevLocs(node);
		if (node.hasFirstParent())
			prevFileLocs.offer(node.getFirstParent());
		if (node.hasSecondParent())
			prevFileLocs.offer(node.getSecondParent());
		return true;
	}
	
	private void updatePrevLocs(ChangedFileNode node) {
		List<ChangedFileLocation> res = new ArrayList<ChangedFileLocation>();
		Revision r = node.getRev().getRevision();
		int prevCount = node.getChangedFile().getPreviousVersionsCount();
		if (r.getParentsCount() == 1 && prevCount > 1) {
			ChangedFileLocation prevLoc = findPrevious(node.getChangedFile(), r.getParents(0));
			if (prevLoc != null)
				res.add(prevLoc);
		} else if (r.getParentsCount() == 1 && prevCount == 1) {
			int revIdx = node.getChangedFile().getPreviousVersions(0);
			int fileIdx = node.getChangedFile().getPreviousIndices(0);
			node.setFirstParent(new ChangedFileLocation(revIdx, fileIdx));
		} else if (r.getParentsCount() == 2) {
			for (int i = 0; i < r.getParentsCount(); i++) {
				ChangedFileLocation prevLoc = findPrevious(node.getChangedFile(), r.getParents(i));
				if (prevLoc != null)
					if (i == 0)
						node.setFirstParent(prevLoc);
					else
						node.setSecondParent(prevLoc);
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
			// first parent in main branch
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
		while (!tree.prevFileLocs.isEmpty()) {
			ChangedFileLocation loc = tree.prevFileLocs.poll();
			if (!fileLocs.contains(loc)) {
				this.prevFileLocs.offer(loc);
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