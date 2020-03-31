package boa.functions.code.change.method;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import boa.functions.code.change.ChangeDataBase;
import boa.functions.code.change.declaration.DeclLocation;
import boa.functions.code.change.declaration.DeclNode;

public class MethodForest {

	private HashMap<Integer, MethodTree> trees;
	public final ChangeDataBase db;
	protected boolean debug = false;

	// considered ref types
	protected HashSet<String> refTypes = new HashSet<String>(
			Arrays.asList(new String[] { "Move Class", "Rename Class" }));

	public MethodForest(ChangeDataBase db, boolean debug) {
		this.db = db;
		this.trees = db.methodForest;
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
						trees.put(tree.getId(), tree);
				}
			}
		}
	}

	public HashMap<Integer, MethodTree> getTrees() {
		return this.trees;
	}

}
