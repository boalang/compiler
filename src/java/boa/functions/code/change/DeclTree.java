package boa.functions.code.change;

import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

import boa.functions.code.change.refactoring.RefactoringBond;

public class DeclTree {
	
	private final DeclChangeForest forest;
	private TreeObjectId id;
	private TreeSet<DeclLocation> declLocs = new TreeSet<DeclLocation>();

	private Queue<FileLocation> prevLocations = new LinkedList<FileLocation>();
	private Queue<String> prevNames = new LinkedList<String>();
	
	public DeclTree(DeclChangeForest forest, DeclNode node, int treeIdx) {
		this.forest = forest;
		this.id = new TreeObjectId(treeIdx);
		add(node);
	}

	private void add(DeclNode node) {
		// check if the node is added by some trees
		
		// update tree
		declLocs.add(node.getLoc());
		// node update tree id
		node.setTreeId(this.id);
		// update global nodes
		forest.fcf.db.declLocToNode.put(node.getLoc(), node);
		
		// update prev queues
		for (int refBondIdx : node.getFileNode().getLeftRefBonds().getClassLevel()) {
			RefactoringBond rb = forest.fcf.db.refBonds.get(refBondIdx);
			if (forest.refTypes.contains(rb.getType())
					&& node.getSignature().equals(rb.getRightElement())) {
				prevLocations.add((FileLocation) rb.getLeftLoc());
				prevNames.add(rb.getLeftElement());
			}
		}
	}

	public boolean linkAll() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public TreeSet<DeclLocation> getDeclLocs() {
		return declLocs;
	}
	
	
}
