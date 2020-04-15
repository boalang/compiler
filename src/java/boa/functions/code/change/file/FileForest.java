package boa.functions.code.change.file;

import static boa.functions.BoaAstIntrinsics.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import boa.functions.code.change.ASTChange;
import boa.functions.code.change.ChangeDataBase;
import boa.functions.code.change.RefactoringBond;
import boa.functions.code.change.RefactoringBonds;
import boa.functions.code.change.RefactoringConnector;
import boa.functions.code.change.RevNode;
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Code.CodeRefactoring;
import boa.types.Shared.ChangeKind;
import boa.types.Toplevel.Project;

public class FileForest {

	private HashMap<Integer, FileTree> trees;
	public final ChangeDataBase db;
	protected boolean debug = false;

	public FileForest(ChangeDataBase db, boolean debug) {
		this.db = db;
		this.trees = db.fileForest;
		this.debug = debug;
		buildTrees();
	}

	private void buildTrees() {
		for (int i = db.revIdxMap.size() - 1; i >= 0; i--) {
			RevNode r = db.revIdxMap.get(i);
			for (FileNode fn : r.getFileChangeMap().values()) {
				if (!db.fileDB.containsKey(fn.getLoc())) {
					if (debug)
						System.err.println("start new node " + fn.getLoc());
					FileTree tree = new FileTree(this, fn, trees.size());
					if (tree.linkAll())
						trees.put(tree.getId(), tree);
				}
			}
		}
	}

	public HashMap<Integer, FileTree> getTrees() {
		return trees;
	}

	// refactoring functions
	public void updateWithRefs(Project p, HashSet<String> refRevIds) throws Exception {
		RefactoringConnector cnn = new RefactoringConnector(db);
		for (String id : refRevIds) {
			RevNode r = db.revIdMap.get(id);
			List<CodeRefactoring> refs = getCodeChange(p, r.getRevision()).getRefactoringsList();
			RefactoringBonds bonds = new RefactoringBonds();
			for (CodeRefactoring ref : refs) {
				if (db.refTypes.contains(ref.getType())) {
					RefactoringBond bond = new RefactoringBond(ref);
					bonds.add(bond, db.refDB.size());
					db.refDB.add(bond);
				}
			}
			cnn.connect(bonds, r);
		}
	}

	private HashSet<FileLocation> visited = new HashSet<FileLocation>();

	// update ast changes
	public void updateASTChanges() throws Exception {
		ASTChange astChange = new ASTChange(db);
		DeclCollector collector = new DeclCollector();
		for (Entry<FileLocation, FileNode> e : db.fileDB.descendingMap().entrySet()) {
			FileNode fn = e.getValue();
			if (visited.contains(fn.getLoc()))
				continue;
			Queue<FileNode> queue = new LinkedList<FileNode>();
			queue.offer(fn);
			while (!queue.isEmpty()) {
				FileNode rightNode = queue.poll();
//				System.out.println(rightNode.getLoc());
				visited.add(rightNode.getLoc());

				// edge case: added file w/o any further modifications
				if (rightNode.getChangedFile().getChange() == ChangeKind.ADDED && rightNode.getASTChangeCount() == 0) {
					astChange.update(rightNode, collector.getDeclNodes(rightNode), ChangeKind.ADDED, true);
				}

				// update changes from 1st parent
				if (rightNode.hasFirstParent()) {
					FileNode leftNode = rightNode.getFirstParent();
					astChange.compare(leftNode, rightNode, collector, true);
					queue.offer(leftNode);
					rightNode.setFirstChange(rightNode.getChangedFile().getChange());
				} else {
					rightNode.setFirstChange(ChangeKind.ADDED);
				}

				// update changes from 2nd parent
				if (rightNode.hasSecondParent()) {
					FileNode leftNode = rightNode.getSecondParent();
					astChange.compare(leftNode, rightNode, collector, false);
					queue.offer(leftNode);
					ChangeKind change = leftNode.getChangedFile().getObjectId()
							.equals(rightNode.getChangedFile().getObjectId()) ? ChangeKind.COPIED : ChangeKind.MODIFIED;
					rightNode.setSecondChange(change);
				}
			}
		}
	}

	public class DeclCollector extends BoaAbstractVisitor {
		private List<Declaration> nodes;

		@Override
		public boolean preVisit(final Declaration node) throws Exception {
			nodes.add(node);
			for (Declaration d : node.getNestedDeclarationsList())
				visit(d);
			return false;
		}

		public List<Declaration> getDeclNodes(FileNode fn) throws Exception {
			this.nodes = new ArrayList<Declaration>();
			this.visit(fn.getChangedFile());
			return nodes;
		}
	}
}
