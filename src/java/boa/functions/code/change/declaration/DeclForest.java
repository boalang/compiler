package boa.functions.code.change.declaration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import boa.functions.code.change.ChangeDataBase;
import boa.functions.code.change.file.FileLocation;
import boa.functions.code.change.file.FileNode;

public class DeclForest {

	protected List<DeclTree> trees = new ArrayList<DeclTree>();
	protected ChangeDataBase db;
	protected boolean debug = false;

	// considered ref types
	protected HashSet<String> refTypes = new HashSet<String>(
			Arrays.asList(new String[] { "Move Class", "Rename Class" }));

	public DeclForest(ChangeDataBase db, boolean debug) throws Exception {
		this.db = db;
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
					DeclTree tree = new DeclTree(this, declNode, trees.size());
					if (tree.linkAll())
						trees.add(tree);
				}
			}
		}
	}

	public List<DeclTree> getTreesAsList() {
		return this.trees;
	}
}
