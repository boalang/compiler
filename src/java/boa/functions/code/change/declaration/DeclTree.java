package boa.functions.code.change.declaration;

import java.util.Stack;
import java.util.TreeSet;

import boa.functions.code.change.TreeObjectId;
import boa.functions.code.change.file.ChangedFileNode;

public class DeclTree {

	private final DeclChangeForest forest;
	private TreeObjectId id;
	private TreeSet<ChangedDeclLocation> declLocs = new TreeSet<ChangedDeclLocation>();
	private Stack<ChangedDeclNode> prevNodes = new Stack<ChangedDeclNode>();

	public DeclTree(DeclChangeForest forest, ChangedDeclNode node, int treeIdx) {
		this.forest = forest;
		this.id = new TreeObjectId(treeIdx);
		add(node);
	}

	public boolean linkAll() {
		while (!prevNodes.isEmpty()) {
			ChangedDeclNode node = prevNodes.pop();
			if (this.forest.debug)
				System.out.println("pop parent " + node.getLoc());
			if (!add(node))
				return false;
		}
		return true;
	}

	private boolean add(ChangedDeclNode node) {
		if (forest.debug)
			System.out.println("try to add node " + node.getLoc() + " to list " + this.id);
		// case 1: check if the node is added by some trees
		if (forest.db.declDB.containsKey(node.getLoc())) {
			int listIdx = forest.db.declDB.get(node.getLoc()).getTreeId().getAsInt();
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

	private void updatePrevNodes(ChangedDeclNode node) {
		ChangedFileNode fn = node.getFileNode();
		if (fn.hasFirstParent()) {
			if (forest.debug)
				System.out.println("file node " + fn.getLoc() + " has 1st parent " + fn.getFirstParent().getLoc());
			ChangedDeclNode firstParent = findPreviousNode(node, fn.getFirstParent());
			node.setFirstParent(firstParent);
		}
		if (fn.hasSecondParent()) {
			if (forest.debug)
				System.out.println("file node " + fn.getLoc() + " has 2nd parent " + fn.getSecondParent().getLoc());
			ChangedDeclNode secondParent = findPreviousNode(node, fn.getSecondParent());
			node.setSecondParent(secondParent);
		}
	}

	private ChangedDeclNode findPreviousNode(ChangedDeclNode node, ChangedFileNode firstParent) {
		String fqn = node.getSignature();
		if (forest.debug)
			System.out.println("try to find decl name " + fqn);
		ChangedFileNode cur = firstParent;
		while (true) {
			ChangedDeclNode prev = cur.getDeclChange(fqn);
			if (forest.debug)
				System.out.println("file node " + cur.getLoc() + " has map " + cur.getDeclChangeMap()
						+ " with list size " + cur.getDeclChanges().size());
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
			System.out.println("list " + this.id + " merge list " + tree.id);
		// update list id
		this.declLocs.addAll(tree.declLocs);
		// update list id
		tree.id.setId(this.id.getAsInt());
		// merge queues
		while (!tree.prevNodes.isEmpty()) {
			ChangedDeclNode node = tree.prevNodes.pop();
			if (!declLocs.contains(node.getLoc())) {
				this.prevNodes.push(node);
			}
		}
		return this;
	}

	public TreeSet<ChangedDeclLocation> getDeclLocs() {
		return declLocs;
	}

}
