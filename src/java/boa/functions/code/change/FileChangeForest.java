package boa.functions.code.change;

import static boa.functions.BoaAstIntrinsics.*;
import static boa.functions.code.change.refactoring.BoaRefactoringPredictionIntrinsics.getRefactorings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import boa.functions.code.change.refactoring.RefactoringBond;
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Code.CodeRefactoring;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import boa.types.Toplevel.Project;

public class FileChangeForest {

	// tree properties
	protected List<FileTree> trees = new ArrayList<FileTree>();

	protected boolean debug = false;

	// global data
	protected final GlobalData gd;

	public FileChangeForest(GlobalData gd, boolean debug) {
		this.gd = gd;
		this.debug = debug;
		updateTrees();
	}

	private void updateTrees() {
		for (int i = gd.revIdxMap.size() - 1; i >= 0; i--) {
			RevNode r = gd.revIdxMap.get(i);
			for (FileNode fn : r.getJavaFileNodes()) {
				if (!gd.fileLocIdToNode.containsKey(fn.getLoc())) {
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
			RevNode r = gd.revIdMap.get(id);
			List<CodeRefactoring> refs = refTypes == null ? getCodeChange(p, r.getRevision()).getRefactoringsList()
					: getRefactorings(p, r.getRevision(), refTypes);
			for (CodeRefactoring ref : refs) {
				String beforeFilePath = ref.getLeftSideLocations(0).getFilePath();
				FileNode fileBefore = findLastModification(beforeFilePath, r);
				String afterFilePath = ref.getRightSideLocations(0).getFilePath();
				FileNode fileAfter = getFileNodeFrom(afterFilePath, r);
				RefactoringBond refBond = new RefactoringBond(fileBefore.getLoc(), fileAfter.getLoc(), ref);
				int refBondIdx = gd.refBonds.size();
				gd.refBonds.add(refBond);
				// update file ref bonds
				fileBefore.getRightRefBonds().add(refBond, refBondIdx);
				fileAfter.getLeftRefBonds().add(refBond, refBondIdx);
				// update tree ref locations
				int beforeTreeIdx = gd.fileLocIdToNode.get(fileBefore.getLoc()).getTreeObjectId().getAsInt();
				FileTree beforeTree = trees.get(beforeTreeIdx);
				beforeTree.fileBeforeRef.add(fileBefore.getLoc());
				int afterTreeIdx = gd.fileLocIdToNode.get(fileAfter.getLoc()).getTreeObjectId().getAsInt();
				FileTree afterTree = trees.get(afterTreeIdx);
				afterTree.fileBeforeRef.add(fileAfter.getLoc());
			}
		}
	}

	private FileNode findLastModification(String fileName, RevNode r) {
		FileNode fn = getFileNodeFrom(fileName, r);
		// case: extract interface from added file
		if (fn != null && fn.getChangedFile().getChange() == ChangeKind.ADDED) {
			return fn;
		}
		RevNode cur = r;
		do {
			if (cur.getRevision().getParentsCount() == 0)
				return null;
			// first parent in main branch
			cur = gd.revIdxMap.get(cur.getRevision().getParents(0));
			fn = getFileNodeFrom(fileName, cur);
			if (fn != null) {
				return fn;
			}

		} while (true);
	}

	private FileNode getFileNodeFrom(String filePath, RevNode r) {
		for (ChangedFile cf : r.getRevision().getFilesList())
			if (cf.getName().equals(filePath))
				return gd.fileLocIdToNode.get(new FileLocation(cf.getRevisionIdx(), cf.getFileIdx()));
		return null;
	}

	// update code element edges
	public void updateWithEdges() throws Exception {

		DeclarationCollector declCollector = new DeclarationCollector();
		for (Entry<FileLocation, FileNode> e : gd.fileLocIdToNode.descendingMap().entrySet()) {
			FileNode leftNode = e.getValue();
			if (leftNode.getChangedFile().getChange() != ChangeKind.DELETED)
				declCollector.getDeclNodes(leftNode);
			
			
//			if (leftNode.getPrevLocs().size() == 0 
//					&& leftNode.getRightRefBonds().getSize() == 0)
//				continue;
//			
//			FileNode rightNode = gd.fileLocIdToNode.get(leftNode.getPrevLocs().get(0));
			
//			if (fn.getChangedFile().getChange() != ChangeKind.DELETED) {
//				for (DeclarationNode dn : declCollector.getDeclNodes(fn)) {
//					
//				}
//			}
		}
	}

	public class DeclarationCollector extends BoaAbstractVisitor {
		private int declIdx;
		private FileNode fn;
		private List<DeclarationNode> nodes = new ArrayList<DeclarationNode>();

		@Override
		public boolean preVisit(final Declaration node) throws Exception {
			String fqn = node.getFullyQualifiedName();
			DeclarationNode declNode = new DeclarationNode(fn, fqn, declIdx++);
			gd.declLocToNode.put(declNode.getLoc(), declNode);
			nodes.add(declNode);
//			System.out.println(declNode);
			for (Declaration d : node.getNestedDeclarationsList())
				visit(d);
			return false;
		}

		public List<DeclarationNode> getDeclNodes(FileNode fn) throws Exception {
			this.declIdx = 0;
			this.fn = fn;
			this.nodes.clear();
			String oid = fn.getChangedFile().getObjectId();
			if (!gd.fileObjectIdToASTRoot.containsKey(oid))
				gd.fileObjectIdToASTRoot.put(oid, getast(fn.getChangedFile()));
			this.visit(gd.fileObjectIdToASTRoot.get(oid));
			return nodes;
		}
	}
}
