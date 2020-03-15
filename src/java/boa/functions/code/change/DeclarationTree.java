package boa.functions.code.change;

import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

public class DeclarationTree {
	
	private final DeclarationChangeForest forest;
	private TreeObjectId id;
	private TreeSet<DeclarationLocation> declLocs = new TreeSet<DeclarationLocation>();
	private Queue<DeclarationLocation> prevLocations = new LinkedList<DeclarationLocation>();
	
	
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
		forest.fcf.gd.declLocToNode.put(node.getLoc(), node);
		// update prev queues
		
	}
	
	
	
}
