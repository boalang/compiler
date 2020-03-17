package boa.functions.code.change;

import static boa.functions.BoaAstIntrinsics.*;
import static boa.functions.code.change.refactoring.BoaRefactoringPredictionIntrinsics.getRefactorings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
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
	
	private HashSet<FileLocation> visited = new HashSet<FileLocation>();

	// update code element edges
	public void updateWithEdges() throws Exception {
		for (Entry<FileLocation, FileNode> e : gd.fileLocIdToNode.descendingMap().entrySet()) {
			FileNode fn = e.getValue();
			System.out.println(fn.getLoc());
			if (visited.contains(fn.getLoc()))
				continue;
			Queue<FileNode> queue = new LinkedList<FileNode>();
			queue.offer(fn);
			while (!queue.isEmpty()) {
				FileNode leftNode = queue.poll();
				visited.add(leftNode.getLoc());
				for (FileLocation loc : leftNode.getPrevLocs()) {
					FileNode rightNode = gd.fileLocIdToNode.get(loc);
					compareFileNodes(rightNode, leftNode);
					if (rightNode.getPrevLocs().size() > 0) {
						queue.offer(rightNode);
						visited.add(rightNode.getLoc());
					}
				}
			}
		}
	}

	private void compareFileNodes(FileNode rightNode, FileNode leftNode) throws Exception {
		DeclarationCollector declCollector = new DeclarationCollector();
		List<Declaration> rightDecls = null;
		
		// left is deleted
		if (leftNode.getChangedFile().getChange() == ChangeKind.DELETED) {
			if (rightDecls == null)
				rightDecls = declCollector.getDeclNodes(rightNode);
			ASTChange leftASTChange = new ASTChange();
			for (int i = 0; i < rightDecls.size(); i++) {
				Declaration decl = rightDecls.get(i);
				DeclarationNode declNode = new DeclarationNode(rightNode, decl.getFullyQualifiedName(), i, ChangeKind.DELETED);
				leftASTChange.getDecls().add(declNode);
				for (int j = 0; j < decl.getMethodsCount(); j++) {
					Method method = decl.getMethods(j);
					MethodNode methodNode = new MethodNode(declNode, method.getName(), j, ChangeKind.DELETED);
					leftASTChange.getMethods().add(methodNode);
				}
				for (int k = 0; k < decl.getFieldsCount(); k++) {
					Variable var = decl.getFields(k);
					FieldNode varNode = new FieldNode(declNode, var.getName(), k, ChangeKind.DELETED);
					leftASTChange.getFields().add(varNode);
				}
			}
			leftNode.getAstChanges().add(leftASTChange);
		}
		
		// right is added
		if (rightNode.getChangedFile().getChange() == ChangeKind.ADDED 
				&& rightNode.getPrevLocs().size() == 0) {
			if (rightDecls == null)
				rightDecls = declCollector.getDeclNodes(rightNode);
			ASTChange rightASTChange = new ASTChange();
			for (int i = 0; i < rightDecls.size(); i++) {
				Declaration decl = rightDecls.get(i);
				DeclarationNode declNode = new DeclarationNode(rightNode, decl.getFullyQualifiedName(), i, ChangeKind.ADDED);
				rightASTChange.getDecls().add(declNode);
				for (int j = 0; j < decl.getMethodsCount(); j++) {
					Method method = decl.getMethods(j);
					MethodNode methodNode = new MethodNode(declNode, method.getName(), j, ChangeKind.ADDED);
					rightASTChange.getMethods().add(methodNode);
				}
				for (int k = 0; k < decl.getFieldsCount(); k++) {
					Variable var = decl.getFields(k);
					FieldNode varNode = new FieldNode(declNode, var.getName(), k, ChangeKind.ADDED);
					rightASTChange.getFields().add(varNode);
				}
			}
			rightNode.getAstChanges().add(rightASTChange);
		}

		// change is saved into left node
		if (leftNode.getChangedFile().getChange() == ChangeKind.MODIFIED) {
			if (rightDecls == null)
				rightDecls = declCollector.getDeclNodes(rightNode);
			List<Declaration> leftDecls = declCollector.getDeclNodes(leftNode);
			ASTChange leftASTChange = new ASTChange();
			
			for (int i = 0; i < rightDecls.size(); i++) {
				Declaration rightDecl = rightDecls.get(i);
				boolean found = false;
				for (int j = 0; j < leftDecls.size(); j++) {
					Declaration leftDecl = leftDecls.get(j);
					if (rightDecl.getFullyQualifiedName().equals(leftDecl.getFullyQualifiedName())) {
						
						found = true;
						break;
					}
					
				}
				if (!found) {
					System.out.println("deleted class " + rightDecl.getFullyQualifiedName());
					DeclarationNode declNode = new DeclarationNode(rightNode, rightDecl.getFullyQualifiedName(), i, ChangeKind.DELETED);
					leftASTChange.getDecls().add(declNode);
				}
			}
			
			if (leftASTChange.getSize() != 0) {
				leftNode.getAstChanges().add(leftASTChange);
			}
		}
		
	}

	public class DeclarationCollector extends BoaAbstractVisitor {
		private List<Declaration> nodes = new ArrayList<Declaration>();

		@Override
		public boolean preVisit(final Declaration node) throws Exception {
			nodes.add(node);
			for (Declaration d : node.getNestedDeclarationsList())
				visit(d);
			return false;
		}

		public List<Declaration> getDeclNodes(FileNode fn) throws Exception {
			this.nodes.clear();
			this.visit(fn.getChangedFile());
			return nodes;
		}
	}
}
