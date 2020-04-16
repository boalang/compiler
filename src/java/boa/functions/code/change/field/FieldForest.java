package boa.functions.code.change.field;

import java.util.HashMap;
import java.util.Map.Entry;

import boa.functions.code.change.ChangeDataBase;
import boa.functions.code.change.declaration.DeclLocation;
import boa.functions.code.change.declaration.DeclNode;

public class FieldForest {

	private HashMap<Integer, FieldTree> trees;
	private int treeId = 0;
	public final ChangeDataBase db;
	protected boolean debug = false;

	public FieldForest(ChangeDataBase db, boolean debug) {
		this.db = db;
		this.trees = db.fieldForest;
		this.debug = debug;
		buildTrees();
	}

	private void buildTrees() {
		for (Entry<DeclLocation, DeclNode> e : db.declDB.descendingMap().entrySet()) {
			DeclNode dn = e.getValue();
			for (FieldNode n : dn.getFieldChanges()) {
				if (!db.fieldDB.containsKey(n.getLoc())) {
					if (debug)
						System.out.println("start new node " + n.getLoc());
					FieldTree tree = new FieldTree(this, n, treeId++);
					if (tree.linkAll())
						trees.put(tree.getId(), tree);
				}
			}
		}
	}

	public HashMap<Integer, FieldTree> getTrees() {
		return this.trees;
	}

}
