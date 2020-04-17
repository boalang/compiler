package boa.functions.code.change.file;

import java.util.HashSet;
import java.util.Stack;
import java.util.TreeSet;

import boa.functions.code.change.RevNode;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

public class FileTree {

	private final FileForest forest;
	private int id;
	private TreeSet<FileNode> fileNodes = new TreeSet<FileNode>();
	private Stack<FileNode> prevNodes = new Stack<FileNode>();
	// refactoring info
	public HashSet<FileLocation> fileBeforeRef = new HashSet<FileLocation>();
	public HashSet<FileLocation> fileAfterRef = new HashSet<FileLocation>();

	public FileTree(FileForest fileChangeForest, FileNode node, int treeIdx) {
		this.forest = fileChangeForest;
		this.id = treeIdx;
		add(node);
	}

	public boolean linkAll() {
		while (!prevNodes.isEmpty()) {
			FileNode node = prevNodes.pop();
			if (this.forest.debug)
				System.out.println("pop parent " + node.getLoc());
			if (!add(node))
				return false;
		}
		return true;
	}

	private boolean add(FileNode node) {
		if (forest.debug)
			System.out.println("try to add node " + node.getLoc() + " " + node.getChangedFile().getChange()
					+ " to list " + this.id);
		// case 1: check if the node is added by some trees
		if (node.getTreeId() != -1) {
			if (node.getTreeId() != id) {
				if (forest.debug)
					System.out.println("node " + node.getLoc() + " already added to list " + node.getTreeId());
				int thisId = id;
				forest.getTrees().get(node.getTreeId()).merge(this).linkAll();
				if (forest.debug)
					System.out.println("drop list " + thisId);
				return false;
			}
			if (forest.debug)
				System.out.println("node " + node.getLoc() + " already in the same tree");
			return true;
		}

		// case 2: update tree
		fileNodes.add(node);
		// node update tree id
		node.setint(this.id);
		// update global nodes
		forest.db.fileDB.put(node.getLoc(), node);
		forest.db.fileNames.add(node.getChangedFile().getName());
		// update RevNode
//		node.getRev().getFileChangeMap().put(node.getChangedFile().getName(), node); // TODO
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

	private void updatePrevNodes(FileNode node) {
		for (int i = 0; i < node.getRev().getRevision().getParentsCount(); i++) {
			// if node's first change is added then stop searching first parent
			if (i == 0 && node.getChangedFile().getChange() == ChangeKind.ADDED)
				continue;
			FileNode prevNode = getPreviousNode(node, i);
//			FileNode prevNode = findPreviousNode(node.getChangedFile(), node.getRev().getRevision().getParents(i));
			if (prevNode != null) {
				// check if the prevNode is already added to database
				if (forest.db.fileDB.containsKey(prevNode.getLoc()))
					prevNode = forest.db.fileDB.get(prevNode.getLoc());
				if (i == 0)
					node.setFirstParent(prevNode);
				else
					node.setSecondParent(prevNode);
			}
		}
	}

	private FileNode getPreviousNode(FileNode node, int i) {
		ChangedFile cf = node.getChangedFile();
		Revision r = node.getRev().getRevision();
		int prevContentCount = node.getChangedFile().getPreviousVersionsCount();
		// if a file has only one parent and previous content locations has size 1
		if (r.getParentsCount() == 1 && prevContentCount == 1) {
			int revIdx = node.getChangedFile().getPreviousVersions(0);
			int fileIdx = node.getChangedFile().getPreviousIndices(0);
			return forest.db.revIdxMap.get(revIdx).getFileNode(fileIdx);
		}
		return findPreviousNode(cf, r.getParents(i));
	}

	// find previous file from parent r
	private FileNode findPreviousNode(ChangedFile cf, int revParentIdx) {
		String prevName = cf.getChange() == ChangeKind.RENAMED ? cf.getPreviousNames(0) : cf.getName();
		RevNode cur = forest.db.revIdxMap.get(revParentIdx);
		do {
			FileNode node = cur.getFileNode(prevName);
			if (node != null)
				return node;
			if (cur.getRevision().getParentsCount() == 0)
				return null;
			// check first-parent branch
			cur = forest.db.revIdxMap.get(cur.getRevision().getParents(0));
		} while (true);
	}

	public FileTree merge(FileTree tree) {
		if (forest.debug)
			System.out.println("list " + this.id + " merge list " + tree.id);
		// remove tree
		forest.getTrees().remove(tree.getId());

		// merge all nodes from tree
		for (FileNode fn : tree.getFileNodes()) {
			fn.setTreeId(this.id);
			this.fileNodes.add(fn);
		}
		// merge queues
		while (!tree.prevNodes.isEmpty()) {
			FileNode node = tree.prevNodes.pop();
			if (!fileNodes.contains(node))
				this.prevNodes.push(node);
		}
		return this;
	}

	public int getId() {
		return id;
	}

	public TreeSet<FileNode> getFileNodes() {
		return fileNodes;
	}
}