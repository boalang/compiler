package boa.functions.code.change.file;

import static boa.functions.BoaAstIntrinsics.*;
import static boa.functions.code.change.refactoring.BoaRefactoringPredictionIntrinsics.getRefactorings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Queue;

import boa.functions.code.change.ASTChange;
import boa.functions.code.change.ChangeDataBase;
import boa.functions.code.change.RevNode;
import boa.functions.code.change.refactoring.RefactoringBond;
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Code.CodeRefactoring;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import boa.types.Toplevel.Project;

public class FileChangeForest {

	private List<FileTree> trees = new ArrayList<FileTree>();
	public final ChangeDataBase db;
	protected boolean debug = false;

	public FileChangeForest(ChangeDataBase gd, boolean debug) {
		this.db = gd;
		this.debug = debug;
		buildTrees();
	}

	private void buildTrees() {
		for (int i = db.revIdxMap.size() - 1; i >= 0; i--) {
			RevNode r = db.revIdxMap.get(i);
			for (ChangedFileNode fn : r.getJavaFileNodes()) {
				if (!db.fileDB.containsKey(fn.getLoc())) {
					if (debug)
						System.err.println("start new node " + fn.getLoc());
					FileTree tree = new FileTree(this, fn, trees.size());
					if (tree.linkAll())
						trees.add(tree);
				}
			}
		}
	}

	public List<FileTree> getTreesAsList() {
		return this.trees;
	}

	// refactoring functions
	public void updateWithRefs(Project p, HashSet<String> refRevIds, Set<String> refTypes) {
		for (String id : refRevIds) {
			RevNode r = db.revIdMap.get(id);
			List<CodeRefactoring> refs = refTypes == null ? getCodeChange(p, r.getRevision()).getRefactoringsList()
					: getRefactorings(p, r.getRevision(), refTypes);
			for (CodeRefactoring ref : refs) {
				String beforeFilePath = ref.getLeftSideLocations(0).getFilePath();
				ChangedFileNode fileBefore = findLastModification(beforeFilePath, r);
				String afterFilePath = ref.getRightSideLocations(0).getFilePath();
				ChangedFileNode fileAfter = getFileNodeFrom(afterFilePath, r);
				RefactoringBond refBond = new RefactoringBond(fileBefore.getLoc(), fileAfter.getLoc(), ref);
				int refBondIdx = db.refDB.size();
				db.refDB.add(refBond);
				// update file ref bonds
				fileBefore.getRightRefBonds().add(refBond, refBondIdx);
				fileAfter.getLeftRefBonds().add(refBond, refBondIdx);
				// update tree ref locations
				int beforeTreeIdx = db.fileDB.get(fileBefore.getLoc()).getTreeId().getAsInt();
				FileTree beforeTree = trees.get(beforeTreeIdx);
				beforeTree.fileBeforeRef.add(fileBefore.getLoc());
				int afterTreeIdx = db.fileDB.get(fileAfter.getLoc()).getTreeId().getAsInt();
				FileTree afterTree = trees.get(afterTreeIdx);
				afterTree.fileBeforeRef.add(fileAfter.getLoc());
			}
		}
	}

	private ChangedFileNode findLastModification(String fileName, RevNode r) {
		ChangedFileNode fn = getFileNodeFrom(fileName, r);
		// case: extract interface from added file
		if (fn != null && fn.getChangedFile().getChange() == ChangeKind.ADDED) {
			return fn;
		}
		RevNode cur = r;
		do {
			if (cur.getRevision().getParentsCount() == 0)
				return null;
			// first parent in main branch
			cur = db.revIdxMap.get(cur.getRevision().getParents(0));
			fn = getFileNodeFrom(fileName, cur);
			if (fn != null) {
				return fn;
			}

		} while (true);
	}

	private ChangedFileNode getFileNodeFrom(String filePath, RevNode r) {
		for (ChangedFile cf : r.getRevision().getFilesList())
			if (cf.getName().equals(filePath))
				return db.fileDB.get(new ChangedFileLocation(cf.getRevisionIdx(), cf.getFileIdx()));
		return null;
	}

	private HashSet<ChangedFileLocation> visited = new HashSet<ChangedFileLocation>();

	// update ast changes
	public void updateASTChanges() throws Exception {
		ASTChange astChange = new ASTChange(db);
		DeclCollector collector = new DeclCollector();
		for (Entry<ChangedFileLocation, ChangedFileNode> e : db.fileDB.descendingMap().entrySet()) {
			ChangedFileNode fn = e.getValue();
			if (visited.contains(fn.getLoc()))
				continue;
			Queue<ChangedFileNode> queue = new LinkedList<ChangedFileNode>();
			queue.offer(fn);
			while (!queue.isEmpty()) {
				ChangedFileNode rightNode = queue.poll();
//				System.out.println(rightNode.getLoc());
				visited.add(rightNode.getLoc());

				// edge case: added file w/o any further modifications
				if (rightNode.getChangedFile().getChange() == ChangeKind.ADDED && rightNode.getASTChangeCount() == 0) {
					astChange.update(rightNode, collector.getDeclNodes(rightNode), ChangeKind.ADDED, true);
				}
				
				// update changes from 1st parent
				if (rightNode.hasFirstParent()) {
					ChangedFileNode leftNode = rightNode.getFirstParent();
					astChange.compare(leftNode, rightNode, collector, true);
					queue.offer(leftNode);
					rightNode.setFirstChange(rightNode.getChangedFile().getChange());
				}

				// update changes from 2nd parent
				if (rightNode.hasSecondParent()) {
					ChangedFileNode leftNode = rightNode.getSecondParent();
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

		public List<Declaration> getDeclNodes(ChangedFileNode fn) throws Exception {
			this.nodes = new ArrayList<Declaration>();
			this.visit(fn.getChangedFile());
			return nodes;
		}
	}
}
