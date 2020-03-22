package boa.functions.code.change.field;

import java.util.Stack;
import java.util.TreeSet;
import boa.functions.code.change.TreeObjectId;
import boa.functions.code.change.declaration.DeclNode;

public class FieldTree {

	private final FieldForest forest;
	private TreeObjectId id;
	private TreeSet<FieldLocation> fieldLocs = new TreeSet<FieldLocation>();
	private Stack<FieldNode> prevNodes = new Stack<FieldNode>();

	public FieldTree(FieldForest forest, FieldNode node, int treeIdx) {
		this.forest = forest;
		this.id = new TreeObjectId(treeIdx);
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
			System.out.println("try to add node " + node.getLoc() + " to list " + this.id);
		// case 1: check if the node is added by some trees
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
		fieldLocs.add(node.getLoc());
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
		if (dn.hasFirstParent()) {
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
			System.out.println("try to find decl name " + fqn);
		DeclNode cur = firstParent;
		while (true) {
			FieldNode prev = cur.getFieldChange(fqn);
			if (forest.debug)
				System.out.println("file node " + cur.getLoc() + " has map " + cur.getMethodChangeMap()
						+ " with list size " + cur.getMethodChanges().size());
			if (prev != null) {
				return prev;
			}
			if (!cur.hasFirstParent())
				return null;
			// check first-parent branch
			cur = cur.getFirstParent();
		}
	}

	private FieldTree merge(FieldTree tree) {
		if (forest.debug)
			System.out.println("list " + this.id + " merge list " + tree.id);
		// update list id
		this.fieldLocs.addAll(tree.fieldLocs);
		// update list id
		tree.id.setId(this.id.getAsInt());
		// merge queues
		while (!tree.prevNodes.isEmpty()) {
			FieldNode node = tree.prevNodes.pop();
			if (!fieldLocs.contains(node.getLoc())) {
				this.prevNodes.push(node);
			}
		}
		return this;
	}

	public TreeSet<FieldLocation> getMethodLocs() {
		return fieldLocs;
	}

	
}
