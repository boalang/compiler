package boa.functions.code.change.field;

import java.util.Stack;
import java.util.TreeSet;
import boa.functions.code.change.declaration.DeclNode;
import boa.types.Shared.ChangeKind;

public class FieldTree {

	private final FieldForest forest;
	private int id;
	private TreeSet<FieldNode> fieldNodes = new TreeSet<FieldNode>();
	private Stack<FieldNode> prevNodes = new Stack<FieldNode>();

	public FieldTree(FieldForest forest, FieldNode node, int treeIdx) {
		this.forest = forest;
		this.id = treeIdx;
		add(node);
	}

	public boolean linkAll() {
		while (!prevNodes.isEmpty()) {
			FieldNode node = prevNodes.pop();
			if (this.forest.debug)
				System.out.println("pop parent " + node.getLoc());
			if (!add(node))
				return false;
		}
		return true;
	}

	private boolean add(FieldNode node) {
		if (forest.debug)
			System.out.println("try to add node " + node.getLoc() + " to tree " + this.id);
		// case 1: check if the node is added by some trees
		if (node.getTreeId() != -1) {
			if (node.getTreeId() != id) {
				if (forest.debug)
					System.out.println("node " + node.getLoc() + " already added to tree " + node.getTreeId());
				int thisId = id;
				forest.getTrees().get(node.getTreeId()).merge(this).linkAll();
				if (forest.debug)
					System.out.println("drop tree " + thisId);
				return false;
			}
			if (forest.debug)
				System.out.println("node " + node.getLoc() + " already in the same tree");
			return true;
		}

		// case 2: update tree
		fieldNodes.add(node);
		// node update tree id
		node.setTreeId(this.id);
		// update global nodes
		forest.db.fieldDB.put(node.getLoc(), node);
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

	private void updatePrevNodes(FieldNode node) {
		DeclNode dn = node.getDeclNode();
		// if node's first change is added then stop searching first parent
		if (dn.hasFirstParent() && node.getFirstChange() != ChangeKind.ADDED) {
			if (forest.debug)
				System.out.println("file node " + dn.getLoc() + " has 1st parent " + dn.getFirstParent().getLoc());
			FieldNode firstParent = findPreviousNode(node, dn.getFirstParent());
			node.setFirstParent(firstParent);
		}
		if (dn.hasSecondParent()) {
			if (forest.debug)
				System.out.println("file node " + dn.getLoc() + " has 2nd parent " + dn.getSecondParent().getLoc());
			FieldNode secondParent = findPreviousNode(node, dn.getSecondParent());
			node.setSecondParent(secondParent);
		}
	}

	private FieldNode findPreviousNode(FieldNode node, DeclNode firstParent) {
		String fqn = node.getSignature();
		if (forest.debug)
			System.out.println("try to find field name " + fqn);
		DeclNode cur = firstParent;
		while (true) {
			FieldNode prev = cur.getFieldChange(fqn);
			if (forest.debug)
				System.out.println("file node " + cur.getLoc() + " has map " + cur.getMethodChangeMap()
						+ " with tree size " + cur.getMethodChanges().size());
			if (prev != null) {
				return prev;
			}
			if (!cur.hasFirstParent())
				return null;
			// check first-parent branch
			cur = cur.getFirstParent();
		}
	}

	public FieldTree merge(FieldTree tree) {
		if (forest.debug)
			System.out.println("tree " + this.id + " merge tree " + tree.id);
		// remove tree
		forest.getTrees().remove(tree.getId());
		// merge all nodes from tree
		for (FieldNode fn : tree.getFieldNodes()) {
			fn.setTreeId(this.id);
			this.fieldNodes.add(fn);
		}
		// merge queues
		while (!tree.prevNodes.isEmpty()) {
			FieldNode node = tree.prevNodes.pop();
			if (!fieldNodes.contains(node)) {
				this.prevNodes.push(node);
			}
		}
		return this;
	}

	public TreeSet<FieldNode> getFieldNodes() {
		return fieldNodes;
	}

	public int getId() {
		return id;
	}

}
