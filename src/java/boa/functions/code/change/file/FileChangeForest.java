package boa.functions.code.change.file;

import static boa.functions.BoaAstIntrinsics.*;
import static boa.functions.code.change.refactoring.BoaRefactoringPredictionIntrinsics.getRefactorings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Queue;

import boa.functions.code.change.ASTChange;
import boa.functions.code.change.ChangeDataBase;
import boa.functions.code.change.RevNode;
import boa.functions.code.change.declaration.ChangedDeclNode;
import boa.functions.code.change.field.ChangedFieldNode;
import boa.functions.code.change.method.ChangedMethodNode;
import boa.functions.code.change.refactoring.RefactoringBond;
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Ast.Method;
import boa.types.Ast.Variable;
import boa.types.Code.CodeRefactoring;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import boa.types.Toplevel.Project;

public class FileChangeForest {

	// tree properties
	protected List<FileTree> trees = new ArrayList<FileTree>();

	protected boolean debug = false;

	// global data
	public final ChangeDataBase db;

	public FileChangeForest(ChangeDataBase gd, boolean debug) {
		this.db = gd;
		this.debug = debug;
		updateTrees();
	}

	private void updateTrees() {
		for (int i = db.revIdxMap.size() - 1; i >= 0; i--) {
			RevNode r = db.revIdxMap.get(i);
			for (ChangedFileNode fn : r.getJavaFileNodes()) {
				if (!db.fileLocIdToNode.containsKey(fn.getLoc())) {
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
				int refBondIdx = db.refBonds.size();
				db.refBonds.add(refBond);
				// update file ref bonds
				fileBefore.getRightRefBonds().add(refBond, refBondIdx);
				fileAfter.getLeftRefBonds().add(refBond, refBondIdx);
				// update tree ref locations
				int beforeTreeIdx = db.fileLocIdToNode.get(fileBefore.getLoc()).getTreeObjectId().getAsInt();
				FileTree beforeTree = trees.get(beforeTreeIdx);
				beforeTree.fileBeforeRef.add(fileBefore.getLoc());
				int afterTreeIdx = db.fileLocIdToNode.get(fileAfter.getLoc()).getTreeObjectId().getAsInt();
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
				return db.fileLocIdToNode.get(new ChangedFileLocation(cf.getRevisionIdx(), cf.getFileIdx()));
		return null;
	}

	private HashSet<ChangedFileLocation> visited = new HashSet<ChangedFileLocation>();

	// update code element edges
	public void updateWithEdges() throws Exception {
		
		ASTChange astChange = new ASTChange(db);
		DeclCollector collector = new DeclCollector();
		for (Entry<ChangedFileLocation, ChangedFileNode> e : db.fileLocIdToNode.descendingMap().entrySet()) {
			ChangedFileNode fn = e.getValue();
			if (visited.contains(fn.getLoc()))
				continue;
			Queue<ChangedFileNode> queue = new LinkedList<ChangedFileNode>();
			queue.offer(fn);
			while (!queue.isEmpty()) {
				ChangedFileNode rightNode = queue.poll();
//				System.out.println(rightNode.getLoc());
				visited.add(rightNode.getLoc());
				int prevIdx = 0;
				for (ChangedFileLocation loc : rightNode.getPrevLocs()) {
					// check null
					if (loc != null) {
						ChangedFileNode leftNode = db.fileLocIdToNode.get(loc);
						astChange.compare(leftNode, rightNode, collector, prevIdx);
						queue.offer(leftNode);
					}
					prevIdx++;
				}
				// corner case: node w/t previous one
				if (rightNode.getPrevLocs().size() == 0 && rightNode.getASTChangeCount() == 0) {
					astChange.update(rightNode, collector.getDeclNodes(rightNode), ChangeKind.ADDED);
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
