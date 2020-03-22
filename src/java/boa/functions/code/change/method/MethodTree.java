package boa.functions.code.change.method;

import java.util.Stack;
import java.util.TreeSet;

import boa.functions.code.change.TreeObjectId;
import boa.functions.code.change.declaration.DeclNode;

public class MethodTree {

	private final MethodForest forest;
	private TreeObjectId id;
	private TreeSet<MethodLocation> methodLocs = new TreeSet<MethodLocation>();
	private Stack<MethodNode> prevNodes = new Stack<MethodNode>();

	public MethodTree(MethodForest methodForest, MethodNode node, int treeIdx) {
		this.forest = methodForest;
		this.id = new TreeObjectId(treeIdx);
		add(node);
	}

	public boolean linkAll() {
		while (!prevNodes.isEmpty()) {
			MethodNode node = prevNodes.pop();
			if (this.forest.debug)
				System.out.println("pop parent " + node.getLoc());
			if (!add(node))
				return false;
		}
		return true;
	}

	private boolean add(MethodNode node) {
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
		methodLocs.add(node.getLoc());
		// node update tree id
		node.setTreeId(this.id);
		// update global nodes
		forest.db.methodDB.put(node.getLoc(), node);
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

	private void updatePrevNodes(MethodNode node) {
		DeclNode dn = node.getDeclNode();
		if (dn.hasFirstParent()) {
			if (forest.debug)
				System.out.println("file node " + dn.getLoc() + " has 1st parent " + dn.getFirstParent().getLoc());
			MethodNode firstParent = findPreviousNode(node, dn.getFirstParent());
			node.setFirstParent(firstParent);
		}
		if (dn.hasSecondParent()) {
			if (forest.debug)
				System.out.println("file node " + dn.getLoc() + " has 2nd parent " + dn.getSecondParent().getLoc());
			MethodNode secondParent = findPreviousNode(node, dn.getSecondParent());
			node.setSecondParent(secondParent);
		}
	}

	private MethodNode findPreviousNode(MethodNode node, DeclNode firstParent) {
		String fqn = node.getSignature();
		if (forest.debug)
			System.out.println("try to find decl name " + fqn);
		DeclNode cur = firstParent;
		while (true) {
			MethodNode prev = cur.getMethodChange(fqn);
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

	private MethodTree merge(MethodTree tree) {
		if (forest.debug)
			System.out.println("list " + this.id + " merge list " + tree.id);
		// update list id
		this.methodLocs.addAll(tree.methodLocs);
		// update list id
		tree.id.setId(this.id.getAsInt());
		// merge queues
		while (!tree.prevNodes.isEmpty()) {
			MethodNode node = tree.prevNodes.pop();
			if (!methodLocs.contains(node.getLoc())) {
				this.prevNodes.push(node);
			}
		}
		return this;
	}

	public TreeSet<MethodLocation> getMethodLocs() {
		return methodLocs;
	}

}
