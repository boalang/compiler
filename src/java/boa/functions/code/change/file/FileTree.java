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
	private Stack<ChangedFileNode> prevNodes = new Stack<ChangedFileNode>();
	// refactoring info
	public HashSet<ChangedFileLocation> fileBeforeRef = new HashSet<ChangedFileLocation>();
	public HashSet<ChangedFileLocation> fileAfterRef = new HashSet<ChangedFileLocation>();

	public FileTree(FileChangeForest fileChangeForest, ChangedFileNode node, int treeIdx) {
		this.forest = fileChangeForest;
		this.id = new TreeObjectId(treeIdx);
		add(node);
	}

	public boolean linkAll() {
		while (!prevNodes.isEmpty()) {
			ChangedFileNode node = prevNodes.pop();
			if (this.forest.debug)
				System.out.println("pop parent " + node.getLoc());
			if (!add(node))
				return false;
		}
		return true;
	}

	private boolean add(ChangedFileNode node) {
		if (forest.debug)
			System.out.println("try to add node " + node.getLoc() + " " + node.getChangedFile().getChange()
					+ " to list " + this.id);
		// case 1: check if the node is added by some trees
//		if (forest.db.fileDB.containsKey(node.getLoc())) {
//			int listIdx = forest.db.fileDB.get(node.getLoc()).getTreeId().getAsInt();
		if (node.getTreeId() != null) {
			int listIdx = node.getTreeId().getAsInt();
			if (listIdx != this.id.getAsInt()) {
				if (forest.debug)
					System.out.println("node " + node.getLoc() + " already added to list " + listIdx);
				String thisId = id.toString();
				forest.getTreesAsList().get(listIdx).merge(this).linkAll();
				if (forest.debug)
					System.out.println("drop list " + thisId);
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
		updatePrevNodes(node);
		// push 2nd parent first for dfs first-parent branch first
		if (node.hasSecondParent()) {
			prevNodes.push(node.getSecondParent());
			if (forest.debug)
				System.out.println("push 2nd parent " + node.getSecondParent().getLoc());
		}
		if (node.hasFirstParent()) {
			prevNodes.push(node.getFirstParent());
			if (forest.debug)
				System.out.println("push 1st parent " + node.getFirstParent().getLoc());
		}
		return true;
	}

	private void updatePrevNodes(ChangedFileNode node) {
//		Revision r = node.getRev().getRevision();
//		int prevCount = node.getChangedFile().getPreviousVersionsCount();
//		if (forest.debug)
//			System.out.println("node " + node.getLoc() + " has revision parents: " + r.getParentsCount()
//					+ " and conetent parents: " + prevCount);
//		if (r.getParentsCount() == 1 && prevCount > 1) {
//			ChangedFileNode prevNode = findPreviousNode(node.getChangedFile(), r.getParents(0));
//			if (prevNode != null)
//				node.setFirstParent(prevNode);
//		} else if (r.getParentsCount() == 1 && prevCount == 1) {
//			int revIdx = node.getChangedFile().getPreviousVersions(0);
//			int fileIdx = node.getChangedFile().getPreviousIndices(0);
//			RevNode prevRev = forest.db.revIdxMap.get(revIdx);
//			node.setFirstParent(new ChangedFileNode(prevRev.getRevision().getFiles(fileIdx), prevRev));
//		} else if (r.getParentsCount() == 2) {
//			for (int i = 0; i < r.getParentsCount(); i++) {
//				ChangedFileNode prevNode = findPreviousNode(node.getChangedFile(), r.getParents(i));
//				if (prevNode != null)
//					if (i == 0)
//						node.setFirstParent(prevNode);
//					else
//						node.setSecondParent(prevNode);
//			}
//		}

		for (int i = 0; i < node.getRev().getRevision().getParentsCount(); i++) {
			ChangedFileNode prevNode = getPreviousNode(node, i);
			if (prevNode != null) {
				// check if the prevNode is already added to database
				ChangedFileNode tmp = forest.db.fileDB.get(prevNode.getLoc());
				if (tmp != null)
					prevNode = tmp;
				if (i == 0)
					node.setFirstParent(prevNode);
				else
					node.setSecondParent(prevNode);
			}
		}
	}

	private ChangedFileNode getPreviousNode(ChangedFileNode node, int i) {
		ChangedFile cf = node.getChangedFile();
		Revision r = node.getRev().getRevision();
		int prevContentCount = node.getChangedFile().getPreviousVersionsCount();
//		if (forest.debug)
//			System.out.println("node " + node.getLoc() + " has revision parents: " + r.getParentsCount()
//					+ " and conetent parents: " + prevContentCount);
		// if a file has only one parent and previous content locations has size 1
		if (r.getParentsCount() == 1 && prevContentCount == 1) {
			int revIdx = node.getChangedFile().getPreviousVersions(0);
			int fileIdx = node.getChangedFile().getPreviousIndices(0);
			RevNode prevRev = forest.db.revIdxMap.get(revIdx);
			return new ChangedFileNode(prevRev.getRevision().getFiles(fileIdx), prevRev);
		}
		return findPreviousNode(cf, r.getParents(i));
	}

	// find previous file from parent r
	private ChangedFileNode findPreviousNode(ChangedFile cf, int revParentIdx) {
		String prevName = cf.getChange() == ChangeKind.RENAMED ? cf.getPreviousNames(0) : cf.getName();
		RevNode cur = forest.db.revIdxMap.get(revParentIdx);
		do {
			ChangedFileNode node = getFileNode(prevName, cur);
			if (node != null)
				return node;
			if (cur.getRevision().getParentsCount() == 0)
				return null;
			// check first-parent branch
			cur = forest.db.revIdxMap.get(cur.getRevision().getParents(0));
		} while (true);
	}

	private ChangedFileNode getFileNode(String filePath, RevNode r) {
		for (ChangedFile cf : r.getRevision().getFilesList())
			if (cf.getName().equals(filePath))
				return new ChangedFileNode(cf, r);
		return null;
	}

	public FileTree merge(FileTree tree) {
		if (forest.debug)
			System.out.println("list " + this.id + " merge list " + tree.id);
		// add nodes
		this.fileLocs.addAll(tree.fileLocs);
		// update list id
		tree.id.setId(this.id.getAsInt());
		// merge queues
		while (!tree.prevNodes.isEmpty()) {
			ChangedFileNode node = tree.prevNodes.pop();
			if (!fileLocs.contains(node.getLoc()))
				this.prevNodes.push(node);
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