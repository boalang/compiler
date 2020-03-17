package boa.functions.code.change;

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

import boa.functions.code.change.refactoring.RefactoringBond;
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.ASTRoot;
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
	protected final ChangeDataBase db;

	public FileChangeForest(ChangeDataBase gd, boolean debug) {
		this.db = gd;
		this.debug = debug;
		updateTrees();
	}

	private void updateTrees() {
		for (int i = db.revIdxMap.size() - 1; i >= 0; i--) {
			RevNode r = db.revIdxMap.get(i);
			for (FileNode fn : r.getJavaFileNodes()) {
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
				FileNode fileBefore = findLastModification(beforeFilePath, r);
				String afterFilePath = ref.getRightSideLocations(0).getFilePath();
				FileNode fileAfter = getFileNodeFrom(afterFilePath, r);
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
			cur = db.revIdxMap.get(cur.getRevision().getParents(0));
			fn = getFileNodeFrom(fileName, cur);
			if (fn != null) {
				return fn;
			}

		} while (true);
	}

	private FileNode getFileNodeFrom(String filePath, RevNode r) {
		for (ChangedFile cf : r.getRevision().getFilesList())
			if (cf.getName().equals(filePath))
				return db.fileLocIdToNode.get(new FileLocation(cf.getRevisionIdx(), cf.getFileIdx()));
		return null;
	}

	private HashSet<FileLocation> visited = new HashSet<FileLocation>();

	// update code element edges
	public void updateWithEdges() throws Exception {
		for (Entry<FileLocation, FileNode> e : db.fileLocIdToNode.descendingMap().entrySet()) {
			FileNode fn = e.getValue();
//			System.out.println(fn.getLoc());
			if (visited.contains(fn.getLoc()))
				continue;
			Queue<FileNode> queue = new LinkedList<FileNode>();
			queue.offer(fn);
			while (!queue.isEmpty()) {
				FileNode rightNode = queue.poll();
				visited.add(rightNode.getLoc());
				for (FileLocation loc : rightNode.getPrevLocs()) {
					FileNode leftNode = db.fileLocIdToNode.get(loc);
					compareFileNodes(leftNode, rightNode);
					if (leftNode.getPrevLocs().size() > 0)
						queue.offer(leftNode);
					else
						visited.add(leftNode.getLoc());
				}
			}
		}
	}

	private void compareFileNodes(FileNode leftNode, FileNode rightNode) throws Exception {
		DeclarationCollector declCollector = new DeclarationCollector();
		List<Declaration> leftDecls = null;

		// right is deleted
		if (rightNode.getChangedFile().getChange() == ChangeKind.DELETED) {
			if (leftDecls == null)
				leftDecls = declCollector.getDeclNodes(leftNode);
			ASTChange rightASTChange = new ASTChange();
			for (int i = 0; i < leftDecls.size(); i++) {
				Declaration decl = leftDecls.get(i);
				updateAllASTChange(rightNode, decl, i, ChangeKind.DELETED, rightASTChange);
			}
			rightNode.getAstChanges().add(rightASTChange);
		}

		// left is added
		if (leftNode.getChangedFile().getChange() == ChangeKind.ADDED && leftNode.getPrevLocs().size() == 0) {
			if (leftDecls == null)
				leftDecls = declCollector.getDeclNodes(leftNode);
			ASTChange leftASTChange = new ASTChange();
			for (int i = 0; i < leftDecls.size(); i++) {
				Declaration decl = leftDecls.get(i);
				updateAllASTChange(leftNode, decl, i, ChangeKind.ADDED, leftASTChange);
			}
			leftNode.getAstChanges().add(leftASTChange);
		}

		// change is saved into left node
		if (rightNode.getChangedFile().getChange() == ChangeKind.MODIFIED) {
			if (leftDecls == null)
				leftDecls = declCollector.getDeclNodes(leftNode);
			List<Declaration> rightDecls = declCollector.getDeclNodes(rightNode);
			ASTChange rightASTChange = new ASTChange();

//			if (leftDecls.size() == 0) {
//				System.out.println("left " + leftNode + " " + leftNode.getChangedFile().getChange());
//			}
//			if (rightDecls.size() == 0) {
//				System.out.println("right " + rightNode + " " + rightNode.getChangedFile().getChange());
//			}

			Set<Integer> deleted = Stream.iterate(0, n -> n + 1).limit(leftDecls.size()).collect(Collectors.toSet());
			Set<Integer> added = Stream.iterate(0, n -> n + 1).limit(rightDecls.size()).collect(Collectors.toSet());

			for (int i = 0; i < leftDecls.size(); i++) {
				Declaration leftDecl = leftDecls.get(i);
				for (int j = 0; j < rightDecls.size(); j++) {
					if (added.contains(j)) {
						Declaration rightDecl = rightDecls.get(j);
						if (leftDecl.getFullyQualifiedName().equals(rightDecl.getFullyQualifiedName())) {
							deleted.remove(i);
							added.remove(j);
							compareDeclarations(leftDecl, rightDecl, j, rightNode, rightASTChange);
							break;
						}
					}
				}
			}
			for (int i : deleted) {
				Declaration decl = leftDecls.get(i);
				updateAllASTChange(rightNode, decl, i, ChangeKind.DELETED, rightASTChange);
			}
			for (int j : added) {
				Declaration decl = rightDecls.get(j);
				updateAllASTChange(rightNode, decl, j, ChangeKind.ADDED, rightASTChange);
			}
			if (rightASTChange.getSize() != 0) {
				rightNode.getAstChanges().add(rightASTChange);
			}
		}

	}

	private void updateAllASTChange(FileNode fileNode, Declaration decl, int i, ChangeKind change,
			ASTChange astChange) {
		DeclNode declNode = new DeclNode(fileNode, decl.getFullyQualifiedName(), i, change);
		astChange.getDecls().add(declNode);
		for (int j = 0; j < decl.getMethodsCount(); j++) {
			updateMethodChange(decl, j, declNode, astChange, change);
		}
		for (int k = 0; k < decl.getFieldsCount(); k++) {
			updateFieldChange(decl, k, declNode, astChange, change);
		}
	}

	private void updateFieldChange(Declaration decl, int k, DeclNode declNode, ASTChange astChange,
			ChangeKind change) {
		Variable var = decl.getFields(k);
		FieldNode varNode = new FieldNode(declNode, var.getName(), k, change);
		astChange.getFields().add(varNode);
	}

	private void updateMethodChange(Declaration decl, int j, DeclNode declNode, ASTChange astChange,
			ChangeKind change) {
		Method method = decl.getMethods(j);
		MethodNode methodNode = new MethodNode(declNode, method.getName(), j, change);
		astChange.getMethods().add(methodNode);
	}

	private void compareDeclarations(Declaration leftDecl, Declaration rightDecl, int rightDeclIdx, FileNode rightNode,
			ASTChange rightASTChange) {

		// compare fields
		Set<Integer> deleted = Stream.iterate(0, n -> n + 1).limit(leftDecl.getFieldsCount()).collect(Collectors.toSet());
		Set<Integer> added = Stream.iterate(0, n -> n + 1).limit(rightDecl.getFieldsCount()).collect(Collectors.toSet());
		Set<Integer> modified = new HashSet<Integer>();

		for (int i = 0; i < leftDecl.getFieldsCount(); i++) {
			Variable leftVar = leftDecl.getFields(i);
			for (int j = 0; j < rightDecl.getFieldsCount(); j++) {
				if (added.contains(j)) {
					Variable rightVar = rightDecl.getFields(j);
					if (leftVar.getName().equals(rightVar.getName())) {
						if (!prettyprint(leftVar).equals(prettyprint(rightVar)))
							modified.add(j);
						deleted.remove(i);
						added.remove(j);
						break;
					}
				}
			}
		}

		if (deleted.size() + added.size() == 0)
			return;
		DeclNode declNode = new DeclNode(rightNode, rightDecl.getFullyQualifiedName(), rightDeclIdx,
				ChangeKind.MODIFIED);

		for (int i : deleted) {
			updateFieldChange(leftDecl, i, declNode, rightASTChange, ChangeKind.DELETED);
		}
		for (int j : added) {
			updateFieldChange(rightDecl, j, declNode, rightASTChange, ChangeKind.ADDED);
		}

	}

	public class DeclarationCollector extends BoaAbstractVisitor {
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
