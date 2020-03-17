package boa.functions.code.change;

import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

import boa.functions.code.change.refactoring.RefactoringBond;

public class DeclarationTree {
	
	private final DeclarationChangeForest forest;
	private TreeObjectId id;
	private TreeSet<DeclarationLocation> declLocs = new TreeSet<DeclarationLocation>();

	private Queue<FileLocation> prevLocations = new LinkedList<FileLocation>();
	private Queue<String> prevNames = new LinkedList<String>();
	
	public DeclarationTree(DeclarationChangeForest forest, DeclarationNode node, int treeIdx) {
		this.forest = forest;
		this.id = new TreeObjectId(treeIdx);
		add(node);
	}

	private void add(DeclarationNode node) {
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
	
	public TreeSet<DeclarationLocation> getDeclLocs() {
		return declLocs;
	}
	
	
}
