package boa.functions.code.change.declaration;

import java.util.HashMap;
import java.util.Map.Entry;

import boa.functions.code.change.ChangeDataBase;
import boa.functions.code.change.file.FileLocation;
import boa.functions.code.change.file.FileNode;

public class DeclForest {

	protected HashMap<Integer, DeclTree> trees;
	private int treeId = 0;
	protected ChangeDataBase db;
	protected boolean debug = false;

	public DeclForest(ChangeDataBase db, boolean debug) throws Exception {
		this.db = db;
		this.trees = db.declForest;
		this.debug = debug;
		this.buildTrees();
	}

	private void buildTrees() throws Exception {
		for (Entry<FileLocation, FileNode> e : db.fileDB.descendingMap().entrySet()) {
			FileNode fn = e.getValue();
			for (DeclNode declNode : fn.getDeclChanges()) {
				if (!db.declDB.containsKey(declNode.getLoc())) {
					if (debug)
						System.out.println("start new node " + declNode.getLoc());
					DeclTree tree = new DeclTree(this, declNode, treeId++);
					if (tree.linkAll())
						trees.put(tree.getId(), tree);
				}
			}
		}
	}

	public HashMap<Integer, DeclTree> getTrees() {
		return this.trees;
	}
}
