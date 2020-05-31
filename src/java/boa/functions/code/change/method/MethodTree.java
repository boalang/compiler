package boa.functions.code.change.method;

import java.util.Stack;
import java.util.TreeSet;

import boa.functions.code.change.ASTChangeTree;
import boa.functions.code.change.declaration.DeclNode;
import boa.types.Shared.ChangeKind;

public class MethodTree extends ASTChangeTree {

	private final MethodForest forest;
	private int id;
	private TreeSet<MethodNode> methodNodes = new TreeSet<MethodNode>();
	private TreeSet<MethodNode> refNodes = new TreeSet<MethodNode>();
	private Stack<MethodNode> prevNodes = new Stack<MethodNode>();

	public MethodTree(MethodForest methodForest, MethodNode node, int treeIdx) {
		this.forest = methodForest;
		this.id = treeIdx;
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
		methodNodes.add(node);
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
		// if node's first change is added then stop searching first parent
		if (dn.hasFirstParent() && node.getFirstChange() != ChangeKind.ADDED) {
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
			System.out.println("try to find method name " + fqn);
		DeclNode cur = firstParent;
		while (true) {
			MethodNode prev = cur.getMethodChange(fqn);
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

	public MethodTree merge(MethodTree tree) {
		if (tree.getId() == this.id) {
			System.out.println("same MethodTree");
			return this;
		}
		if (forest.debug)
			System.out.println("tree " + this.id + " merge tree " + tree.id);
		// remove tree
		forest.getTrees().remove(tree.getId());
		// merge all nodes from tree
		for (MethodNode mn : tree.getMethodNodes()) {
			mn.setTreeId(this.id);
			this.methodNodes.add(mn);
		}
		// merge queues
		while (!tree.prevNodes.isEmpty()) {
			MethodNode node = tree.prevNodes.pop();
			if (!methodNodes.contains(node)) {
				this.prevNodes.push(node);
			}
		}
		// merge refactoring nodes
		this.refNodes.addAll(tree.refNodes);
		return this;
	}
	
	public TreeSet<MethodNode> getRefNodes() {
		return refNodes;
	}

	public TreeSet<MethodNode> getMethodNodes() {
		return methodNodes;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
