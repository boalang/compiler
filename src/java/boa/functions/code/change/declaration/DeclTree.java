package boa.functions.code.change.declaration;

import java.util.Stack;
import java.util.TreeSet;

import boa.functions.code.change.TreeObjectId;
import boa.functions.code.change.file.ChangedFileLocation;
import boa.functions.code.change.file.ChangedFileNode;
import boa.types.Shared.ChangeKind;

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
			if (!add(node))
				return false;
		}
		return true;
	}

	private boolean add(ChangedDeclNode node) {
		// case 1: check if the node is added by some trees
		if (forest.db.declDB.containsKey(node.getLoc())) {
			int listIdx = forest.db.declDB.get(node.getLoc()).getTreeId().getAsInt();
			if (listIdx != this.id.getAsInt()) {
				forest.getTreesAsList().get(listIdx).merge(this).linkAll();
				return false;
			}
			return true;
		}
		
		// case 2: update tree
		declLocs.add(node.getLoc());
		// node update tree id
		node.setTreeId(this.id);
		// update global nodes
		forest.db.declDB.put(node.getLoc(), node);
		// update prev queues
		updatePrevLocs(node);
		// push 2nd parent first for dfs first-parent branch first
		if (node.hasSecondParent()) {
			prevNodes.push(node.getSecondParent());			
		}
		if (node.hasFirstParent()) {
			prevNodes.push(node.getFirstParent());
		}
		return true;
	}

	private void updatePrevLocs(ChangedDeclNode node) {
		ChangedFileNode fn = node.getFileNode();
		if (fn.hasFirstParentLoc()) {
			ChangedDeclNode firstParent = findPrevious(node, fn.getFirstParentLoc());
			node.setFirstParent(firstParent);
		}
		if (fn.hasSecondParentLoc()) {
			ChangedDeclNode secondParent = findPrevious(node, fn.getSecondParentLoc());
			node.setSecondParent(secondParent);
		}
	}

	private ChangedDeclNode findPrevious(ChangedDeclNode node, ChangedFileLocation firstParentLoc) {
		String fqn = node.getSignature();
		ChangedFileNode cur = forest.db.fileDB.get(firstParentLoc);
		while (true) {
			ChangedDeclNode prev = cur.getDeclChange(fqn);
			if (prev != null) {
				return prev;
			}
			if (!cur.hasFirstParentLoc())
				return null;
			// check first-parent branch
			cur = forest.db.fileDB.get(cur.getFirstParentLoc());
		}
	}

	private DeclTree merge(DeclTree tree) {
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
