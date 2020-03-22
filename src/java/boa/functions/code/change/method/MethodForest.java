package boa.functions.code.change.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import boa.functions.code.change.ChangeDataBase;
import boa.functions.code.change.declaration.DeclLocation;
import boa.functions.code.change.declaration.DeclNode;

public class MethodForest {

	private List<MethodTree> trees = new ArrayList<MethodTree>();
	public final ChangeDataBase db;
	protected boolean debug = false;

	// considered ref types
	protected HashSet<String> refTypes = new HashSet<String>(
			Arrays.asList(new String[] { "Move Class", "Rename Class" }));

	public MethodForest(ChangeDataBase gd, boolean debug) {
		this.db = gd;
		this.debug = debug;
		buildTrees();
	}

	private void buildTrees() {
		for (Entry<DeclLocation, DeclNode> e : db.declDB.descendingMap().entrySet()) {
			DeclNode dn = e.getValue();
			for (MethodNode mn : dn.getMethodChanges()) {
				if (!db.methodDB.containsKey(mn.getLoc())) {
					if (debug)
						System.out.println("start new node " + mn.getLoc());
					MethodTree tree = new MethodTree(this, mn, trees.size());
					if (tree.linkAll())
						trees.add(tree);
				}
			}
		}
	}

	public List<MethodTree> getTreesAsList() {
		return this.trees;
	}

}
