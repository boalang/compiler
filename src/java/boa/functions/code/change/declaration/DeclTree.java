package boa.functions.code.change.declaration;

import java.util.Stack;
import java.util.TreeSet;

import boa.functions.code.change.TreeObjectId;
import boa.functions.code.change.file.FileNode;

public class DeclTree {

	private final DeclForest forest;
	private TreeObjectId id;
	private TreeSet<DeclLocation> declLocs = new TreeSet<DeclLocation>();
	private Stack<DeclNode> prevNodes = new Stack<DeclNode>();

	public DeclTree(DeclForest forest, DeclNode node, int treeIdx) {
		this.forest = forest;
		this.id = new TreeObjectId(treeIdx);
		add(node);
	}

	public boolean linkAll() {
		while (!prevNodes.isEmpty()) {
			DeclNode node = prevNodes.pop();
			if (this.forest.debug)
				System.out.println("pop parent " + node.getLoc());
			if (!add(node))
				return false;
		}
		return true;
	}

	private boolean add(DeclNode node) {
		if (forest.debug)
			System.out.println("try to add node " + node.getLoc() + " to tree " + this.id);
		// case 1: check if the node is added by some trees
		if (node.getTreeId() != null) {
			int treeIdx = node.getTreeId().getAsInt();
			if (treeIdx != this.id.getAsInt()) {
				if (forest.debug)
					System.out.println("node " + node.getLoc() + " already added to tree " + treeIdx);
				String thisId = id.toString();
				forest.getTreesAsList().get(treeIdx).merge(this).linkAll();
				if (forest.debug)
					System.out.println("drop tree " + thisId);
				return false;
			}
			if (forest.debug)
				System.out.println("node " + node.getLoc() + " already in the same tree");
			return true;
		}

		// case 2: update tree
		declLocs.add(node.getLoc());
		// node update tree id
		node.setTreeId(this.id);
		// update global nodes
		forest.db.declDB.put(node.getLoc(), node);
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

	private void updatePrevNodes(DeclNode node) {
		FileNode fn = node.getFileNode();
		if (fn.hasFirstParent()) {
			if (forest.debug)
				System.out.println("file node " + fn.getLoc() + " has 1st parent " + fn.getFirstParent().getLoc());
			DeclNode firstParent = findPreviousNode(node, fn.getFirstParent());
			node.setFirstParent(firstParent);
		}
		if (fn.hasSecondParent()) {
			if (forest.debug)
				System.out.println("file node " + fn.getLoc() + " has 2nd parent " + fn.getSecondParent().getLoc());
			DeclNode secondParent = findPreviousNode(node, fn.getSecondParent());
			node.setSecondParent(secondParent);
		}
	}

	private DeclNode findPreviousNode(DeclNode node, FileNode firstParent) {
		String fqn = node.getSignature();
		if (forest.debug)
			System.out.println("try to find decl name " + fqn);
		FileNode cur = firstParent;
		while (true) {
			DeclNode prev = cur.getDeclChange(fqn);
			if (forest.debug)
				System.out.println("file node " + cur.getLoc() + " has map " + cur.getDeclChangeMap()
						+ " with tree size " + cur.getDeclChanges().size());
			if (prev != null) {
				return prev;
			}
			if (!cur.hasFirstParent())
				return null;
			// check first-parent branch
			cur = cur.getFirstParent();
		}
	}

	private DeclTree merge(DeclTree tree) {
		if (forest.debug)
			System.out.println("tree " + this.id + " merge tree " + tree.id);
		// update tree id
		this.declLocs.addAll(tree.declLocs);
		// update tree id
		tree.id.setId(this.id.getAsInt());
		// merge queues
		while (!tree.prevNodes.isEmpty()) {
			DeclNode node = tree.prevNodes.pop();
			if (!declLocs.contains(node.getLoc())) {
				this.prevNodes.push(node);
			}
		}
		return this;
	}

	public TreeSet<DeclLocation> getDeclLocs() {
		return declLocs;
	}

}
