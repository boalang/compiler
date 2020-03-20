package boa.functions.code.change.declaration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import boa.functions.code.change.ChangeDataBase;
import boa.functions.code.change.file.ChangedFileLocation;
import boa.functions.code.change.file.ChangedFileNode;

public class DeclChangeForest {

	protected List<DeclTree> trees = new ArrayList<DeclTree>();
	protected ChangeDataBase db;

	// considered ref types
	protected HashSet<String> refTypes = new HashSet<String>(
			Arrays.asList(new String[] { "Move Class", "Rename Class" }));
	
	public DeclChangeForest(ChangeDataBase db) throws Exception {
		this.db = db;
		this.buildTrees();
	}
	
	private void buildTrees() throws Exception {
		for (Entry<ChangedFileLocation, ChangedFileNode> e : db.fileDB.descendingMap().entrySet()) {
			ChangedFileNode fn = e.getValue();
			
			for (ChangedDeclNode declNode : fn.getDeclChanges()) {
				if (!db.declDB.containsKey(declNode.getLoc())) {
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
