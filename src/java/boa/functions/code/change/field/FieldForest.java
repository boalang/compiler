package boa.functions.code.change.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import boa.functions.code.change.ChangeDataBase;
import boa.functions.code.change.declaration.DeclLocation;
import boa.functions.code.change.declaration.DeclNode;

public class FieldForest {

	private List<FieldTree> trees = new ArrayList<FieldTree>();
	public final ChangeDataBase db;
	protected boolean debug = false;

	// considered ref types
	protected HashSet<String> refTypes = new HashSet<String>(
			Arrays.asList(new String[] { "Move Class", "Rename Class" }));

	public FieldForest(ChangeDataBase db, boolean debug) {
		this.db = db;
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
					FieldTree tree = new FieldTree(this, n, trees.size());
					if (tree.linkAll())
						trees.add(tree);
				}
			}
		}
	}

	public List<FieldTree> getTreesAsList() {
		return this.trees;
	}

}
